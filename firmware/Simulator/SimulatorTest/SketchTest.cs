using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Simulator;
using System.IO;
using System.Linq;
using System.Collections.Generic;
using System.Text;
using System.Globalization;

namespace SimulatorTest
{
    [TestClass]
    public class SketchTest
    {
        private static readonly Sketch sketch = Sketch.Current;
        private static Dictionary<string, RuntimeStats[]> stats;

        private static readonly string TEST_FILES_PATH = Path.Combine("..", "SimulatorTest", "TestFiles");

        private const byte RQ_CONNECTION_REQUEST = 255;
        private const byte MAJOR_VERSION = 0;
        private const byte MINOR_VERSION = 10;
        private const byte KEEP_ALIVE = 7;
        private const byte DEBUG_CONTINUE = 12;

        public TestContext TestContext { get; set; }

        private void IncrementMillisAndExec(int increment, int ticksPerMs = 10)
        {
            int millis = sketch.GetMillis();
            for (int i = 0; i < increment; i++)
            {
                sketch.SetMillis(millis + i);
                Loop(ticksPerMs);
            }
        }

        private void Loop(int n = 10)
        {
            for (int i = 0; i < n; i++) { sketch.Loop(); }
        }

        private void LoadProgram(byte[] bytes)
        {
            sketch.WriteSerial(bytes);
            sketch.Loop(); // Execute 1 loop to load the program
            sketch.WriteSerial(new byte[] { DEBUG_CONTINUE });
        }

        [ClassInitialize]
        public static void ClassInit(TestContext context)
        {
            stats = new Dictionary<string, RuntimeStats[]>();
        }

        [TestInitialize]
        public void Setup()
        {
            sketch.RegisterStats(true);
            sketch.SetMillis(0);
            sketch.Setup();

            /*
             * INFO(Richo): Perform connection request and handshake.
             * Otherwise, when we send a program later we will be rejected.
             */
            sketch.WriteSerial(new byte[]
            {
                RQ_CONNECTION_REQUEST, MAJOR_VERSION, MINOR_VERSION
            });
            sketch.Loop();
            byte handshake = sketch.ReadSerial().Item2[0];
            byte send = (byte)((MAJOR_VERSION + MINOR_VERSION + handshake) % 256);
            sketch.WriteSerial(new byte[] { send });
            sketch.Loop();
            byte ack = sketch.ReadSerial().Item2[0];
            Assert.AreEqual(send, ack);

            TurnOffAllPins();
        }

        [TestCleanup]
        public void TearDown()
        {
            stats[TestContext.TestName] = sketch.Stats.ToArray();

            // INFO(Richo): Make sure we get disconnected before the next test
            int steps = 1000;
            int interval = 10;
            int start = int.MaxValue - steps * interval;
            for (int i = 0; i < steps; i++)
            {
                sketch.SetMillis(start + (i * interval));
                sketch.Loop();
            }
        }

        [ClassCleanup]
        public static void ClassCleanup()
        {
            var path = Path.Combine(TEST_FILES_PATH, "RuntimeStats.csv");
            using (var file = new StreamWriter(path, false, Encoding.UTF8))
            {
                // Write header
                var columns = new[]
                {
                    "", "Loops",
                    "Memory used (bytes)",
                    "Total coroutine resizings"
                };
                file.WriteLine(string.Join(",", columns));

                // Use the same number format to print all stats
                var format = CultureInfo.InvariantCulture.NumberFormat;

                // Write aggregate data
                {
                    file.WriteLine(string.Join(",", new []
                    {
                        "MIN",
                        stats.Min(kvp => kvp.Value.Count()).ToString(format),
                        stats.Min(kvp => kvp.Value.Max(each => each.UsedMemory)).ToString(format),
                        stats.Min(kvp => kvp.Value.Sum(each => each.CoroutineResizeCounter)).ToString(format)
                    }));
                    file.WriteLine(string.Join(",", new []
                    {
                        "MAX",
                        stats.Max(kvp => kvp.Value.Count()).ToString(format),
                        stats.Max(kvp => kvp.Value.Max(each => each.UsedMemory)).ToString(format),
                        stats.Max(kvp => kvp.Value.Sum(each => each.CoroutineResizeCounter)).ToString(format)
                    }));
                    file.WriteLine(string.Join(",", new []
                    {
                        "AVERAGE",
                        stats.Average(kvp => kvp.Value.Count()).ToString(format),
                        stats.Average(kvp => kvp.Value.Max(each => each.UsedMemory)).ToString(format),
                        stats.Average(kvp => kvp.Value.Sum(each => each.CoroutineResizeCounter)).ToString(format)
                    }));
                    file.WriteLine(string.Join(",", new []
                    {
                        "MEDIAN",
                        stats.Median(kvp => kvp.Value.Count()).ToString(format),
                        stats.Median(kvp => kvp.Value.Max(each => each.UsedMemory)).ToString(format),
                        stats.Median(kvp => kvp.Value.Sum(each => each.CoroutineResizeCounter)).ToString(format)
                    }));
                }

                // Write individual test data
                {
                    var lines = stats
                        .OrderBy(kvp => kvp.Key)
                        .Select(kvp => string.Join(",", new []
                        {
                            kvp.Key,
                            kvp.Value.Count().ToString(format),
                            kvp.Value.Max(each => each.UsedMemory).ToString(format),
                            kvp.Value.Sum(each => each.CoroutineResizeCounter).ToString(format)
                        }));
                    foreach (var line in lines)
                    {
                        file.WriteLine(line);
                    }
                }
            }
        }

        private void TurnOffAllPins()
        {
            for (int i = 0; i < 19; i++)
            {
                sketch.SetPinValue(i, 0);
            }
        }

        private byte[] ReadFile(string fileName)
        {
            var str = File.ReadAllText(Path.Combine(TEST_FILES_PATH, fileName));
            return str.Split(new[] { ',', ' ' }, StringSplitOptions.RemoveEmptyEntries)
                .Select(each => byte.Parse(each))
                .ToArray();
        }

        [TestMethod]
        public void Test001TurnOnBytecode()
        {
            LoadProgram(ReadFile(nameof(Test001TurnOnBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test002TurnOffBytecode()
        {
            LoadProgram(ReadFile(nameof(Test002TurnOffBytecode)));
            sketch.SetPinValue(13, 1023);
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test003ReadWriteBytecode()
        {
            LoadProgram(ReadFile(nameof(Test003ReadWriteBytecode)));
            /*
            INFO(Richo):
            The choice of 120 as expected value is not casual. Since analogRead() and
            analogWrite() have different scales (the former goes from 0 to 1023, and 
            latter from 0 to 255) and we internally store the pin values as floats,
            some precision can be lost. So, I chose 120 because it's one of those
            values that keep their precision after being read/write.
            */
            sketch.SetPinValue(15, 120);
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(120, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test004PushBytecode()
        {
            LoadProgram(ReadFile(nameof(Test004PushBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test005PushWithFloatingPointVariable()
        {
            LoadProgram(ReadFile(nameof(Test005PushWithFloatingPointVariable)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(Math.Round(0.2 * 1023), sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test006PopBytecode()
        {
            LoadProgram(ReadFile(nameof(Test006PopBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // The first loop sets the var to 1
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // And now we set the pin
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test007PrimBytecode()
        {
            LoadProgram(ReadFile(nameof(Test007PrimBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test008JZBytecode()
        {
            LoadProgram(ReadFile(nameof(Test008JZBytecode)));
            sketch.SetPinValue(13, 0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test009TickingRate()
        {
            LoadProgram(ReadFile(nameof(Test009TickingRate)));

            sketch.SetMillis(0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(1750);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test010MultipleScriptsWithDifferentTickingRates()
        {
            LoadProgram(ReadFile(nameof(Test010MultipleScriptsWithDifferentTickingRates)));

            sketch.SetMillis(0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            Assert.AreEqual(0, sketch.GetPinValue(15));
            Assert.AreEqual(0, sketch.GetPinValue(9));

            sketch.SetPinValue(15, 120);
            sketch.SetMillis(50);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            Assert.AreEqual(120, sketch.GetPinValue(15));
            Assert.AreEqual(0, sketch.GetPinValue(9));

            sketch.SetMillis(500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            Assert.AreEqual(120, sketch.GetPinValue(15));
            Assert.AreEqual(120, sketch.GetPinValue(9));

            sketch.SetMillis(1500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
            Assert.AreEqual(120, sketch.GetPinValue(15));
            Assert.AreEqual(120, sketch.GetPinValue(9));
        }

        [TestMethod]
        public void Test011YieldInstruction()
        {
            LoadProgram(ReadFile(nameof(Test011YieldInstruction)));

            sketch.SetMillis(0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test012YieldInstructionPreservesStack()
        {
            LoadProgram(ReadFile(nameof(Test012YieldInstructionPreservesStack)));

            sketch.SetMillis(0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test013YieldInstructionResumesOnNextTick()
        {
            LoadProgram(ReadFile(nameof(Test013YieldInstructionResumesOnNextTick)));

            sketch.SetMillis(1000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(12));

            sketch.SetMillis(1001);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(12));

            sketch.SetMillis(1002);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(12));

            sketch.SetMillis(2001);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(12));
        }

        [TestMethod]
        public void Test014PrimitiveYieldTime()
        {
            LoadProgram(ReadFile(nameof(Test014PrimitiveYieldTime)));

            sketch.SetMillis(100);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(101);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1099);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1100);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(1101);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1102);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test015YieldAfterBackwardsJump()
        {
            LoadProgram(ReadFile(nameof(Test015YieldAfterBackwardsJump)));

            sketch.SetMillis(500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(11));
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(11));
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetPinValue(15, 1023);
            sketch.SetMillis(1700);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(11));
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetPinValue(15, 0);
            sketch.SetMillis(1750);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(11));
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1800);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(11));
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(11));
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test016ScriptCallWithoutParametersOrReturnValue()
        {
            LoadProgram(ReadFile(nameof(Test016ScriptCallWithoutParametersOrReturnValue)));

            /*
             * INFO(Richo): This loop allows me to detect stack overflow
             */
            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 500);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (iteration: {0})", i, msg);
                Assert.AreEqual(value, sketch.GetPinValue(10), "D10 should be {1} (iteration: {0})", i, msg);
            }
        }


        [TestMethod]
        public void Test017ScriptCallWithoutParametersWithReturnValueAndExplicitReturn()
        {
            LoadProgram(ReadFile(nameof(Test017ScriptCallWithoutParametersWithReturnValueAndExplicitReturn)));

            /*
             * INFO(Richo): These numbers represent the value of the pin D11 after each iteration
             */
            int[] values = new int[]
            {
                61, 72, 82, 92, 102, 113, 123, 133, 143, 153, 164, 174, 184, 194, 205, 215,
                225, 235, 246, 256, 266, 276, 286, 297, 307, 317, 327, 338, 348, 358, 368,
                379, 389, 399, 409, 419, 430, 440, 450, 460, 471, 481, 491, 501, 512, 522,
                532, 542, 552, 563, 573, 583, 593, 604, 614, 624, 634, 644, 655, 665, 675,
                685, 696, 706, 716, 726, 737, 747, 757, 767, 777, 788, 798, 808, 818, 829,
                839, 849, 859, 870, 880, 890, 900, 910, 921, 931, 941, 951, 962, 972, 982,
                992, 1003, 1013, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023,
                1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023
            };
            for (int i = 0; i < values.Length; i++)
            {
                sketch.SetMillis(i * 100 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (iteration: {0})", i);
                short pinValue = sketch.GetPinValue(11);
                Assert.IsTrue(pinValue >= values[i] - 2,
                    "D11 is smaller than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
                Assert.IsTrue(pinValue <= values[i] + 2,
                    "D11 is larger than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
            }
        }

        [TestMethod]
        public void Test018ScriptTickingWithExplicitReturn()
        {
            LoadProgram(ReadFile(nameof(Test018ScriptTickingWithExplicitReturn)));

            /*
             * INFO(Richo): This loop allows me to detect stack overflow
             */
            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test019ScriptWithYieldBeforeEndOfScript()
        {
            LoadProgram(ReadFile(nameof(Test019ScriptWithYieldBeforeEndOfScript)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 100 + 50);
                Loop();
                sketch.SetMillis(i * 100 + 51);
                Loop();

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (iteration: {0})", i, msg);
                
                sketch.SetMillis(i * 100 + 75);
                Loop();
                sketch.SetMillis(i * 100 + 76);
                Loop();
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test020ScriptCallWithOneParameterAndReturnValue()
        {
            LoadProgram(ReadFile(nameof(Test020ScriptCallWithOneParameterAndReturnValue)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test021ScriptCallWithOneParameterWithoutReturnValue()
        {
            LoadProgram(ReadFile(nameof(Test021ScriptCallWithOneParameterWithoutReturnValue)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test022ScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn()
        {
            LoadProgram(ReadFile(nameof(Test022ScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test023ScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn()
        {
            LoadProgram(ReadFile(nameof(Test023ScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test024ScriptCallWithTwoParametersWithReturnValue()
        {
            LoadProgram(ReadFile(nameof(Test024ScriptCallWithTwoParametersWithReturnValue)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off (iteration: {0})", i);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test025ScriptCallWithRecursiveCall4LevelsDeep()
        {
            LoadProgram(ReadFile(nameof(Test025ScriptCallWithRecursiveCall4LevelsDeep)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on (iteration: {0})", i);
            }
        }

        [TestMethod]
        public void Test026ScriptTickingThatAlsoCallsItself()
        {
            LoadProgram(ReadFile(nameof(Test026ScriptTickingThatAlsoCallsItself)));

            sketch.SetMillis(500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            // Press button
            sketch.SetPinValue(9, 1023);

            sketch.SetMillis(1500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            // Release button
            sketch.SetPinValue(9, 0);

            sketch.SetMillis(1750);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            // By now D11 should have its value increased
            Assert.AreEqual(8, sketch.GetPinValue(11));

            // Press button
            sketch.SetPinValue(9, 1023);

            sketch.SetMillis(3500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            // Release button
            sketch.SetPinValue(9, 0);

            sketch.SetMillis(4500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            // By now D11 should have its value increased
            Assert.AreEqual(16, sketch.GetPinValue(11));


            // Press button
            sketch.SetPinValue(9, 1023);

            sketch.SetMillis(4501);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            // Release button
            sketch.SetPinValue(9, 0);

            sketch.SetMillis(4502);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            // By now D11 should have its value increased
            Assert.AreEqual(32, sketch.GetPinValue(11));

        }

        [TestMethod]
        public void Test027PrimitiveCoroutineShouldReturnTheIndexOfTheActiveScript()
        {
            LoadProgram(ReadFile(nameof(Test027PrimitiveCoroutineShouldReturnTheIndexOfTheActiveScript)));

            Assert.AreEqual(0, sketch.GetPinValue(11));

            sketch.SetMillis(1000);
            sketch.Loop();
            sketch.Loop(); // Run twice because of yield
            Assert.AreEqual(0, sketch.GetPinValue(11));

            sketch.SetMillis(2000);
            sketch.Loop();
            sketch.Loop(); // Run twice because of yield
            Assert.AreEqual(1023, sketch.GetPinValue(11));

            sketch.SetMillis(3000);
            sketch.Loop();
            sketch.Loop(); // Run twice because of yield
            Assert.AreEqual(0, sketch.GetPinValue(11));
        }

        [TestMethod]
        public void Test028PrimitiveBitwiseAnd()
        {
            LoadProgram(ReadFile(nameof(Test028PrimitiveBitwiseAnd)));

            /*
             * INFO(Richo): These numbers represent the value of the pin D11 after each iteration
             */
            int[] values = new int[]
            {
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409, 512, 614, 716, 0, 102, 205, 307, 409, 512, 614, 716, 0,
                102, 205, 307, 409
            };

            sketch.SetMillis(0);
            sketch.Loop();
            for (int i = 0; i < values.Length; i++)
            {
                sketch.SetMillis((i + 1) * 1000 + 50);
                sketch.Loop();
                short pinValue = sketch.GetPinValue(11);
                Assert.IsTrue(pinValue >= values[i] - 2,
                    "D11 is smaller than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
                Assert.IsTrue(pinValue <= values[i] + 2,
                    "D11 is larger than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
            }
        }

        [TestMethod]
        public void Test029PrimitiveBitwiseOr()
        {
            LoadProgram(ReadFile(nameof(Test029PrimitiveBitwiseOr)));

            /*
             * INFO(Richo): These numbers represent the value of the pin D11 after each iteration
             */
            int[] values = new int[]
            {
                10, 31, 31, 51, 51, 72, 72, 92, 92, 113, 113, 133, 133, 153, 153, 174, 174,
                194, 194, 215, 215, 235, 235, 256, 256, 276, 276, 297, 297, 317, 317, 338,
                338, 358, 358, 379, 379, 399, 399, 419, 419, 440, 440, 460, 460, 481, 481,
                501, 501, 522, 522, 542, 542, 563, 563, 583, 583, 604, 604, 624, 624, 644,
                644, 665, 665, 685, 685, 706, 706, 726, 726, 747, 747, 767, 767, 788, 788,
                808, 808, 829, 829, 849, 849, 870, 870, 890, 890, 910, 910, 931, 931, 951,
                951, 972, 972, 992, 992, 1013, 1013
            };

            sketch.SetMillis(0);
            sketch.Loop();
            for (int i = 0; i < values.Length; i++)
            {
                sketch.SetMillis((i + 1) * 1000 + 50);
                sketch.Loop();
                short pinValue = sketch.GetPinValue(11);
                Assert.IsTrue(pinValue >= values[i] - 2,
                    "D11 is smaller than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
                Assert.IsTrue(pinValue <= values[i] + 2,
                    "D11 is larger than expected (iteration: {0}, expected: {1}, actual: {2})",
                    i,
                    values[i],
                    pinValue);
            }
        }

        [TestMethod]
        public void Test030PrimitiveLogicalAnd()
        {
            LoadProgram(ReadFile(nameof(Test030PrimitiveLogicalAnd)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                short pinValue = sketch.GetPinValue(11);
                Assert.AreEqual(1023 * (1 - (i % 2)), pinValue);
                Assert.AreEqual(0, sketch.GetPinValue(13));
            }
        }

        [TestMethod]
        public void Test031PrimitiveLogicalOr()
        {
            LoadProgram(ReadFile(nameof(Test031PrimitiveLogicalOr)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                short pinValue = sketch.GetPinValue(13);
                Assert.AreEqual(1023 * (1 - (i % 2)), pinValue);
                Assert.AreEqual(0, sketch.GetPinValue(11));
            }
        }

        [TestMethod]
        public void Test032StopScriptAndRestartShouldResetPCAndStuff()
        {
            LoadProgram(ReadFile(nameof(Test032StopScriptAndRestartShouldResetPCAndStuff)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off");

                sketch.SetMillis(i * 1000 + 550);
                Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off");
            }
        }

        [TestMethod]
        public void Test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart()
        {
            LoadProgram(ReadFile(nameof(Test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off");
            }
        }

        [TestMethod]
        public void Test034StartOnTheCurrentTaskShouldJumpToTheBeginning()
        {
            LoadProgram(ReadFile(nameof(Test034StartOnTheCurrentTaskShouldJumpToTheBeginning)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on");
            }
        }

        [TestMethod]
        public void Test035StartOnAnotherTaskShouldResetToBeginning()
        {
            LoadProgram(ReadFile(nameof(Test035StartOnAnotherTaskShouldResetToBeginning)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on");
            }
        }

        [TestMethod]
        public void Test036ResumeOnARunningTaskShouldHaveNoEffect()
        {
            LoadProgram(ReadFile(nameof(Test036ResumeOnARunningTaskShouldHaveNoEffect)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 100 + 50);
                Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC()
        {
            LoadProgram(ReadFile(nameof(Test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                Loop();
                Assert.AreEqual(1023 * ((i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test038ResumeOnStoppedTaskShouldJumpToBeginning()
        {
            LoadProgram(ReadFile(nameof(Test038ResumeOnStoppedTaskShouldJumpToBeginning)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on");
            }
        }

        [TestMethod]
        public void Test039StartOnStoppedTaskShouldJumpToBeginning()
        {
            LoadProgram(ReadFile(nameof(Test039StartOnStoppedTaskShouldJumpToBeginning)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on");
            }
        }

        [TestMethod]
        public void Test040StartOnPausedTaskShouldJumpToBeginning()
        {
            LoadProgram(ReadFile(nameof(Test040StartOnPausedTaskShouldJumpToBeginning)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be on");
            }
        }

        [TestMethod]
        public void Test041PausingShouldPreserveTheStack()
        {
            LoadProgram(ReadFile(nameof(Test041PausingShouldPreserveTheStack)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                // INFO(Richo): Run several times to give it the chance of pausing and resuming
                sketch.Loop();
                sketch.Loop();
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test042EmptyScriptShouldNotCrashTheVM()
        {
            LoadProgram(ReadFile(nameof(Test042EmptyScriptShouldNotCrashTheVM)));
            sketch.Loop();
        }

        [TestMethod]
        public void Test043ForLoop()
        {
            LoadProgram(ReadFile(nameof(Test043ForLoop)));

            for (int i = 7; i <= 11; i++)
            {
                sketch.SetMillis(i * 1000);
                Loop();

                for (int j = 7; j <= 11; j++)
                {
                    bool shouldBeOn = j <= i;
                    string msg = string.Format("D{0} should be {1}", j, shouldBeOn ? "on" : "off");
                    Assert.AreEqual(shouldBeOn ? 1023 : 0, sketch.GetPinValue(j), msg);                    
                }
            }
        }

        [TestMethod]
        public void Test044ReversedForLoop()
        {
            LoadProgram(ReadFile(nameof(Test044ReversedForLoop)));

            for (int i = 11; i >= 7; i--)
            {
                sketch.SetMillis((100 - i) * 1000);
                Loop();

                for (int j = 11; j >= 7; j--)
                {
                    bool shouldBeOn = j >= i;
                    string msg = string.Format("D{0} should be {1}", j, shouldBeOn ? "on" : "off");
                    Assert.AreEqual(shouldBeOn ? 1023 : 0, sketch.GetPinValue(j), msg);
                }
            }
        }

        [TestMethod]
        public void Test045ForLoopWithoutConstantStep()
        {
            LoadProgram(ReadFile(nameof(Test045ForLoopWithoutConstantStep)));

            for (int i = 7; i <= 11; i++)
            {
                sketch.SetMillis(i * 1000);
                Loop();

                for (int j = 7; j <= 11; j++)
                {
                    bool shouldBeOn = j <= i;
                    string msg = string.Format("D{0} should be {1}", j, shouldBeOn ? "on" : "off");
                    Assert.AreEqual(shouldBeOn ? 1023 : 0, sketch.GetPinValue(j), msg);
                }
            }
        }

        [TestMethod]
        public void Test046ReverseForLoopWithoutConstantStep()
        {
            LoadProgram(ReadFile(nameof(Test046ReverseForLoopWithoutConstantStep)));

            for (int i = 11; i >= 7; i--)
            {
                sketch.SetMillis((100 - i) * 1000);
                Loop();

                for (int j = 11; j >= 7; j--)
                {
                    bool shouldBeOn = j >= i;
                    string msg = string.Format("D{0} should be {1}", j, shouldBeOn ? "on" : "off");
                    Assert.AreEqual(shouldBeOn ? 1023 : 0, sketch.GetPinValue(j), msg);
                }
            }
        }

        [TestMethod]
        public void Test047ForLoopShouldOnlyEvaluateStepOncePerIteration()
        {
            LoadProgram(ReadFile(nameof(Test047ForLoopShouldOnlyEvaluateStepOncePerIteration)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);

                // Execute the VM loop multiple times in the same ms but the step should be the same anyway
                for (int j = 0; j < 100; j++)
                {
                    sketch.Loop();
                    Assert.AreEqual(1023 * (i % 2), sketch.GetPinValue(13), "D13 should blink on each tick");
                }
            }
        }

        [TestMethod]
        public void Test048MutexShouldGuaranteeACriticalSection()
        {
            LoadProgram(ReadFile(nameof(Test048MutexShouldGuaranteeACriticalSection)));
            /*
                D13 should blink every second.
                D11 should follow the pattern:
                0 s -> 0
                1 s -> 0.25
                2 s -> 0.5
                3 s -> 0.75
                4 s -> 1
                5 s -> 1
                6 s -> 1
                7 s -> 0.5
                8 s -> 0.5
                9 s -> 0
                10 s -> 0
                11 s -> 0
             */
            double[] pattern = new[] { 0, 0.25, 0.5, 0.75, 1, 1, 1, 0.5, 0.5, 0, 0, 0 };
            for (int i = 0; i < 12; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 100; j++)
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
                Assert.AreEqual(pattern[i], sketch.GetPinValue(11) / 1023.0, 0.05, "D11 should be {0}", pattern[i]);
            }
        }

        [TestMethod]
        public void Test049ChannelShouldDeadlockIfConsumingFromTheSameTaskAsProducer()
        {
            LoadProgram(ReadFile(nameof(Test049ChannelShouldDeadlockIfConsumingFromTheSameTaskAsProducer)));
            /*
                D13 should blink every second.
                D11 should always be off.
             */
            for (int i = 0; i < 100; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 100; j++)
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
                Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should always be on");
            }
        }

        [TestMethod]
        public void Test050ChannelWithMultipleProducersAndNoConsumerShouldBlockAllProducers()
        {
            LoadProgram(ReadFile(nameof(Test050ChannelWithMultipleProducersAndNoConsumerShouldBlockAllProducers)));
            /*
                D13 should blink every second.
                D11 should always be 0.5.
             */
            for (int i = 0; i < 100; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 100; j++)
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
                Assert.AreEqual(0.5, sketch.GetPinValue(11) / 1023.0, 0.05, "D11 should always be 0.5");
            }
        }

        [TestMethod]
        public void Test051ChannelWithOneProducerAndOneConsumerBlocksTheProducerAtTheRateOfConsumer()
        {
            LoadProgram(ReadFile(nameof(Test051ChannelWithOneProducerAndOneConsumerBlocksTheProducerAtTheRateOfConsumer)));
            /*
                D13 should blink every second.
                D11 should always be opposite of D13
             */
            for (int i = 0; i < 100; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 900; j++) // TODO(Richo): This works but if the condition is j < 100 it doesn't, why?
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
                Assert.AreEqual(1023 * (i % 2), sketch.GetPinValue(11), "D11 should always be opposite of D13");
            }
        }

        [TestMethod]
        public void Test052ChannelWithMultipleProducersAndOneConsumer()
        {
            LoadProgram(ReadFile(nameof(Test052ChannelWithMultipleProducersAndOneConsumer)));
            /*
                D13 should blink every second.
                D11 should follow the pattern:
                0 s -> 0
                1 s -> 0.25
                2 s -> 0.5
                3 s -> 0.75
                4 s -> 1
                5 s -> 0
             */
            double[] pattern = new[] { 0, 0.25, 0.5, 0.75, 1 };
            for (int i = 0; i < 20; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 100; j++)
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                double d11 = pattern[i % pattern.Length];
                Assert.AreEqual(d11, sketch.GetPinValue(11) / 1023.0, 0.05, "D11 should be {0}", d11);
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
            }
        }

        [TestMethod]
        public void Test053ChannelWithMultipleConsumersAndOneProducer()
        {
            LoadProgram(ReadFile(nameof(Test053ChannelWithMultipleConsumersAndOneProducer)));
            /*
                D10, D11, D12, and D13 should follow the pattern:
                0 s -> D10: 0.00, D11: 0.00, D12: 0.00, D13: 0.00
                1 s -> D10: 0.25, D11: 0.00, D12: 0.00, D13: 0.00
                2 s -> D10: 0.25, D11: 0.50, D12: 0.00, D13: 0.00
                3 s -> D10: 0.25, D11: 0.50, D12: 0.75, D13: 0.00
                4 s -> D10: 0.25, D11: 0.50, D12: 0.75, D13: 1.00
                5 s -> D10: 0.00, D11: 0.50, D12: 0.75, D13: 1.00
                6 s -> D10: 0.00, D11: 0.25, D12: 0.75, D13: 1.00
                7 s -> D10: 0.00, D11: 0.25, D12: 0.50, D13: 1.00
                8 s -> D10: 0.00, D11: 0.25, D12: 0.50, D13: 0.75
             */
            double[][] pattern = new[]
            {
                /*       D10   D11   D12   D13 */
                new[] { 0.00, 0.00, 0.00, 0.00 },
                new[] { 0.25, 0.00, 0.00, 0.00 },
                new[] { 0.25, 0.50, 0.00, 0.00 },
                new[] { 0.25, 0.50, 0.75, 0.00 },
                new[] { 0.25, 0.50, 0.75, 1.00 },
                new[] { 0.00, 0.50, 0.75, 1.00 },
                new[] { 0.00, 0.25, 0.75, 1.00 },
                new[] { 0.00, 0.25, 0.50, 1.00 },
                new[] { 0.00, 0.25, 0.50, 0.75 },
            };
            for (int i = 0; i < pattern.Length; i++)
            {
                // INFO(Richo): I execute the loop multiple times to make sure all the behavior for this second is complete.
                int time = i * 1000;
                for (int j = 0; j < 100; j++)
                {
                    sketch.SetMillis(time++);
                    sketch.Loop();
                }

                double[] pins = pattern[i];
                for (int pinIndex = 0; pinIndex < pins.Length; pinIndex++)
                {
                    double pinExpected = pins[pinIndex];
                    int pinNumber = pinIndex + 10;
                    Assert.AreEqual(pinExpected, sketch.GetPinValue(pinNumber) / 1023.0, 0.05, "D{0} should be {1}", pinNumber, pinExpected);
                }
            }
        }

        [TestMethod]
        public void Test054VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther()
        {
            LoadProgram(ReadFile(nameof(Test054VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(7), "D7 should blink every second");
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(6), "D6 should blink every second");
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off");
            }
        }

        [TestMethod]
        public void Test055VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther()
        {
            LoadProgram(ReadFile(nameof(Test055VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(7), "D7 should blink every second");
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(6), "D6 should blink every second");
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off");
            }
        }

        [TestMethod]
        public void Test056Round()
        {
            LoadProgram(ReadFile(nameof(Test056Round)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should be off");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should be on");
        }

        [TestMethod]
        public void Test057Ceil()
        {
            LoadProgram(ReadFile(nameof(Test057Ceil)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(10), "D10 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should be on");
        }

        [TestMethod]
        public void Test058Floor()
        {
            LoadProgram(ReadFile(nameof(Test058Floor)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should be off");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should be off");
        }

        [TestMethod]
        public void Test059Sqrt()
        {
            LoadProgram(ReadFile(nameof(Test059Sqrt)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.IsTrue(Math.Abs(sketch.GetPinValue(9) - 512) < 5, "D9 should be close to 512");
        }

        [TestMethod]
        public void Test060Abs()
        {
            LoadProgram(ReadFile(nameof(Test060Abs)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(9) - 512) < 5, "D9 should be close to 512");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(10) - 512) < 5, "D10 should be close to 512");
        }

        [TestMethod]
        public void Test061NaturalLogarithm()
        {
            LoadProgram(ReadFile(nameof(Test061NaturalLogarithm)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test062Log10()
        {
            LoadProgram(ReadFile(nameof(Test062Log10)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test063Exp()
        {
            LoadProgram(ReadFile(nameof(Test063Exp)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test064Pow10()
        {
            LoadProgram(ReadFile(nameof(Test064Pow10)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test065IsCloseTo()
        {
            LoadProgram(ReadFile(nameof(Test065IsCloseTo)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should be on");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off");
            Assert.AreEqual(1023, sketch.GetPinValue(10), "D10 should be on");
        }

        [TestMethod]
        public void Test066Asin()
        {
            LoadProgram(ReadFile(nameof(Test066Asin)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test067Acos()
        {
            LoadProgram(ReadFile(nameof(Test067Acos)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test068Atan()
        {
            LoadProgram(ReadFile(nameof(Test068Atan)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test069Power()
        {
            LoadProgram(ReadFile(nameof(Test069Power)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test070IsOn()
        {
            LoadProgram(ReadFile(nameof(Test070IsOn)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
            }
        }

        [TestMethod]
        public void Test071IsOff()
        {
            LoadProgram(ReadFile(nameof(Test071IsOff)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
            }
        }

        [TestMethod]
        public void Test072Mod()
        {
            LoadProgram(ReadFile(nameof(Test072Mod)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should start OFF");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should start OFF");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should start OFF");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should start OFF");

            sketch.SetMillis(1000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should TOGGLE in the first iteration");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should remain OFF in the first iteration");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should remain OFF in the first iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the first iteration");

            sketch.SetMillis(2000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should remain ON in the second iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should TOGGLE in the second iteration");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should remain OFF in the second iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the second iteration");

            sketch.SetMillis(3000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should remain ON in the third iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should remain ON in the third iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should TOGGLE in the third iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the third iteration");

            sketch.SetMillis(4000);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should TOGGLE in the fourth iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should remain ON in the fourth iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should remain ON in the fourth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the fourth iteration");

            sketch.SetMillis(5000);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should remain OFF in the fifth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should TOGGLE in the fifth iteration");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should remain ON in the fourth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the fifth iteration");

            sketch.SetMillis(6000);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should remain OFF in the sixth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should remain OFF in the sixth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should TOGGLE in the sixth iteration");
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should remain OFF in the sixth iteration");
        }

        [TestMethod]
        public void Test073Constrain()
        {
            LoadProgram(ReadFile(nameof(Test073Constrain)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(7), "D7 should be on");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test074RandomInt()
        {
            LoadProgram(ReadFile(nameof(Test074RandomInt)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be ON");
            }
        }

        [TestMethod]
        public void Test075Random()
        {
            LoadProgram(ReadFile(nameof(Test075Random)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should always be ON");
            }
        }

        [TestMethod]
        public void Test076IsEven()
        {
            LoadProgram(ReadFile(nameof(Test076IsEven)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test077IsOdd()
        {
            LoadProgram(ReadFile(nameof(Test077IsOdd)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test078IsPrime()
        {
            LoadProgram(ReadFile(nameof(Test078IsPrime)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test079IsWhole()
        {
            LoadProgram(ReadFile(nameof(Test079IsWhole)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test080IsPositive()
        {
            LoadProgram(ReadFile(nameof(Test080IsPositive)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test081IsNegative()
        {
            LoadProgram(ReadFile(nameof(Test081IsNegative)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test082IsDivisibleBy()
        {
            LoadProgram(ReadFile(nameof(Test082IsDivisibleBy)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test083DelayS()
        {
            LoadProgram(ReadFile(nameof(Test083DelayS)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");

            sketch.SetMillis(500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");

            sketch.SetMillis(1000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");

            sketch.SetMillis(1500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
        }

        [TestMethod]
        public void Test084DelayM()
        {
            LoadProgram(ReadFile(nameof(Test084DelayM)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");

            sketch.SetMillis(500);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");

            sketch.SetMillis(60000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");

            sketch.SetMillis(60500);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
        }

        [TestMethod]
        public void Test085Minutes()
        {
            LoadProgram(ReadFile(nameof(Test085Minutes)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(0.5 * 60000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(1 * 60000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(1.5 * 60000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(2 * 60000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(2.5 * 60000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(3 * 60000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(3.5 * 60000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(4 * 60000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }
        }

        [TestMethod]
        public void Test086Seconds()
        {
            LoadProgram(ReadFile(nameof(Test086Seconds)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(0.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(1 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(1.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(2 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(2.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(3 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(3.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(4 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }
        }

        [TestMethod]
        public void Test087Millis()
        {
            LoadProgram(ReadFile(nameof(Test087Millis)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(0.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(1 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(1.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(2 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(2.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(3 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis((int)(3.5 * 1000) + i);
                sketch.Loop();
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            }

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(4 * 1000 + i);
                sketch.Loop();
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off");
            }
        }


        [TestMethod]
        public void Test088ScriptCallOverridingPrimitive()
        {
            LoadProgram(ReadFile(nameof(Test088ScriptCallOverridingPrimitive)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 500);
                sketch.Loop();
                sketch.Loop();

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test089DebuggerBreakpointHaltsAllScripts()
        {
            LoadProgram(ReadFile(nameof(Test089DebuggerBreakpointHaltsAllScripts)));

            // NOTE(Richo): First, we verify that the program does what it's supposed to do.
            for (int i = 0; i < 100; i++)
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
                IncrementMillisAndExec(1000);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (step: 0, iteration: {0})", i, msg);
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (step: 0, iteration: {0})", i, msg);
            }

            // NOTE(Richo): protocol setBreakpoints: #(12)
            sketch.WriteSerial(new byte[] { 13, 1, 1, 0, 12 });

            // NOTE(Richo): The VM must halt and the pins must be left untouched.
            sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
            IncrementMillisAndExec(1000);

            for (int i = 0; i < 100; i++)
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
                IncrementMillisAndExec(1000);

                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off (step: 1, iteration: {0})", i);
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off (step: 1, iteration: {0})", i);
            }

            // NOTE(Richo): protocol sendContinue
            sketch.WriteSerial(new byte[] { 12 });

            /*
             * NOTE(Richo): The VM must continue one loop and halt again at the same place as before.
             * So the pins are turned on and they must remain on until continue is sent again.
             */
            sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
            IncrementMillisAndExec(1000);

            for (int i = 0; i < 100; i++)
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
                IncrementMillisAndExec(1000);

                Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on (step: 2, iteration: {0})", i);
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on (step: 2, iteration: {0})", i);
            }

            // NOTE(Richo): protocol clearBreakpoints: #(12)
            sketch.WriteSerial(new byte[] { 13, 0, 1, 0, 12 });

            /*
             * NOTE(Richo): Clearing the breakpoints should have no effect in the VM state. The program
             * is still halted and the pins must remain on.
             */
            sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
            IncrementMillisAndExec(1000);

            for (int i = 0; i < 100; i++)
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
                IncrementMillisAndExec(1000);

                Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on (step: 3, iteration: {0})", i);
                Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on (step: 3, iteration: {0})", i);
            }

            // NOTE(Richo): protocol sendContinue
            sketch.WriteSerial(new byte[] { 12 });

            /*
             * NOTE(Richo): The VM can continue execution without interruptions.
             */
            sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
            IncrementMillisAndExec(1000);

            for (int i = 0; i < 100; i++)
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE }); // We need to keep the connection!
                IncrementMillisAndExec(1000);

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (step: 4, iteration: {0})", i, msg);
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (step: 4, iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test090DebuggerBreakpointHaltsAreDeterministic()
        {
            // We need to keep the connection alive, executing this will run the loop once
            Action keepAlive = () =>
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE });
                sketch.Loop();
            };

            LoadProgram(ReadFile(nameof(Test090DebuggerBreakpointHaltsAreDeterministic)));

            int time = 0;
            sketch.SetMillis(time);
            sketch.Loop();
            keepAlive();

            // NOTE(Richo): The program is now in memory. We can check that it works correctly
            var lastD9 = -1;
            for (int i = 0; i < 10000; i++)
            {
                time += 100;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();

                Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off (step: 0, iteration: {0})", i);
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 0, iteration: {0})", i);

                var curD9 = sketch.GetPinValue(9);
                Assert.AreNotEqual(lastD9, curD9, "D9 should have changed from last iteration (step: 0, iteration: {0})", i);
                lastD9 = curD9;

                bool on = i % 2 != 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (step: 0, iteration: {0})", i, msg);
            }

            // NOTE(Richo): We are now ready to put the first breakpoint
            sketch.WriteSerial(new byte[] { 13, 1, 1, 0, 30 });
            sketch.Loop();

            // NOTE(Richo): Execution should be halted now. Let's check...
            lastD9 = sketch.GetPinValue(9);
            for (int i = 0; i < 10000; i++)
            {
                time += 10;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();

                Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off (step: 1, iteration: {0})", i);
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 1, iteration: {0})", i);

                var curD9 = sketch.GetPinValue(9);
                Assert.AreEqual(lastD9, curD9, "D9 should not have changed from last iteration (step: 1, iteration: {0})", i);

                Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should remain turned on (step: 1, iteration: {0})", i);
            }

            // NOTE(Richo): With the VM halted we should turn on D7 and make sure D13 remains off
            sketch.SetPinValue(7, 1023);
            lastD9 = sketch.GetPinValue(9);
            for (int i = 0; i < 10000; i++)
            {
                time += 10;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();

                Assert.AreEqual(1023, sketch.GetPinValue(7), "D7 should be on (step: 2, iteration: {0})", i);
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 2, iteration: {0})", i);

                var curD9 = sketch.GetPinValue(9);
                Assert.AreEqual(lastD9, curD9, "D9 should not have changed from last iteration (step: 2, iteration: {0})", i);

                Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should remain turned on (step: 2, iteration: {0})", i);
            }


            // NOTE(Richo): Now we remove the breakpoint and continue
            sketch.WriteSerial(new byte[] { 13, 0, 1, 0, 30 });
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(7), "D7 should be on (step: 3)");
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 3)");
            sketch.WriteSerial(new byte[] { 12 });
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off (step: 4)");
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 4)");

            // NOTE(Richo): Execution should resume and D13 should continue to be off!
            lastD9 = -1;
            for (int i = 0; i < 1000; i++)
            {
                time += 10;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();

                Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off (step: 5, iteration: {0})", i);
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should always be off (step: 5, iteration: {0})", i);

                var curD9 = sketch.GetPinValue(9);
                Assert.AreNotEqual(lastD9, curD9, "D9 should have changed from last iteration (step: 5, iteration: {0})", i);
                lastD9 = curD9;

                bool on = i % 2 != 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (step: 5, iteration: {0})", i, msg);
            }
        }
        
        [TestMethod]
        public void Test091ChangingTheProgramResetsTheVMState()
        {
            // We need to keep the connection alive, executing this will run the loop once
            Action keepAlive = () =>
            {
                sketch.WriteSerial(new[] { KEEP_ALIVE });
                sketch.Loop();
            };

            LoadProgram(ReadFile(nameof(Test091ChangingTheProgramResetsTheVMState)));

            int time = 0;
            sketch.SetMillis(time);
            sketch.Loop();
            keepAlive();

            // NOTE(Richo): First, we verify that the program does what it's supposed to do.
            for (int i = 0; i < 100; i++)
            {
                time += 1000;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();

                bool on = i % 2 != 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (step: 0, iteration: {0})", i, msg);
            }

            // NOTE(Richo): Now we set a breakpoint on the first instruction
            sketch.WriteSerial(new byte[] { 13, 1, 1, 0, 0 });
            sketch.Loop();
            
            // NOTE(Richo): The program is halted, D13 should not change
            for (int i = 0; i < 100; i++)
            {
                time += 1000;
                sketch.SetMillis(time);
                sketch.Loop();
                keepAlive();
                
                Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off (step: 1, iteration: {0})", i);
            }

            // NOTE(Richo): Now we send the program again. Execution should restart.
            LoadProgram(ReadFile(nameof(Test091ChangingTheProgramResetsTheVMState)));
            sketch.Loop();
            try
            {
                for (int i = 0; i < 100; i++)
                {
                    time += 1000;
                    sketch.SetMillis(time);
                    sketch.Loop();
                    keepAlive();

                    bool on = i % 2 != 0;
                    int value = on ? 1023 : 0;
                    string msg = on ? "on" : "off";
                    Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (step: 2, iteration: {0})", i, msg);
                }
            }
            catch (AssertFailedException)
            {
                // Finally, we remove the breakpoint and continue to avoid messing with the other tests
                sketch.WriteSerial(new byte[] { 13, 0, 1, 0, 0 });
                sketch.Loop();
                sketch.WriteSerial(new byte[] { 12 });
                sketch.Loop();
                throw;
            }
        }

        [TestMethod]
        public void Test092DebuggerSetAllBreakpoints()
        {
            LoadProgram(ReadFile(nameof(Test092DebuggerSetAllBreakpoints)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should start off");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should start off");

            sketch.Loop(); // Reads program and executes one loop
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should remain off after first loop");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should remain off after first loop");

            sketch.WriteSerial(new byte[] { 14, 1 }); // Set breakpoint on all instructions
            sketch.Loop(); // Pauses on the first instruction
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off while halted on instruction 0");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 0");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the second instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 1");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 1");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the third instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 2");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on while halted on instruction 2");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the fourth instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 3");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 3");


            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the firsth instruction again
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off while halted on instruction 1 (again)");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 1 (again)");
        }


        [TestMethod]
        public void Test093DebuggerSetAllBreakpointsWithMultipleScripts()
        {
            LoadProgram(ReadFile(nameof(Test093DebuggerSetAllBreakpointsWithMultipleScripts)));

            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should start off");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should start off");

            sketch.Loop(); // Reads program and executes one loop
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should remain off after first loop");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should remain off after first loop");

            sketch.WriteSerial(new byte[] { 14, 1 }); // Set breakpoint on all instructions
            sketch.Loop(); // Pauses on the first instruction
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off while halted on instruction 0");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 0");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the second instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 1");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 1");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the third instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 2");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on while halted on instruction 2");

            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the fourth instruction
            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on while halted on instruction 3");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 3");


            sketch.WriteSerial(new byte[] { 12 }); // Continue without removing any breakpoint.
            sketch.Loop(); // Pauses on the firsth instruction again
            Assert.AreEqual(0, sketch.GetPinValue(13), "D13 should be off while halted on instruction 1 (again)");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off while halted on instruction 1 (again)");
        }


        [TestMethod]
        public void Test094ProgramWithMultipleImports()
        {
            LoadProgram(ReadFile(nameof(Test094ProgramWithMultipleImports)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink every second");
            }
        }

        [TestMethod]
        public void Test095ScriptWith127Instructions()
        {
            LoadProgram(ReadFile(nameof(Test095ScriptWith127Instructions)));

            int millis = 0;
            for (int i = 0; i < 23; i++)
            {
                sketch.SetMillis(millis += 100);
                Loop(10);

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink (step: 0, iteration: {0})", i);
            }

            IncrementMillisAndExec(1000, 10);

            millis = sketch.GetMillis();
            for (int i = 0; i < 23; i++)
            {
                sketch.SetMillis(millis += 100);
                Loop(10);

                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink (step: 1, iteration: {0})", i);
            }
        }

        [TestMethod]
        public void Test096ScriptWith128Instructions()
        {
            LoadProgram(ReadFile(nameof(Test096ScriptWith128Instructions)));

            int last = sketch.GetPinValue(13);
            int count = 0;
            for (int i = 0; i < 5000; i++)
            {
                IncrementMillisAndExec(100, 1);

                if (sketch.GetPinValue(13) == last) { count++; }
                else { last = sketch.GetPinValue(13); count = 0; }

                Console.WriteLine($"{i} -> {sketch.GetPinValue(13)}");
                Assert.IsTrue(count < 3, "D13 should blink (iteration: {0})", i);
            }
        }

        [TestMethod]
        public void Test097ScriptWith512Instructions()
        {
            LoadProgram(ReadFile(nameof(Test097ScriptWith512Instructions)));

            int last = sketch.GetPinValue(13);
            int count = 0;
            for (int i = 0; i < 1000; i++)
            {
                IncrementMillisAndExec(100, 1);

                if (sketch.GetPinValue(13) == last) { count++; }
                else { last = sketch.GetPinValue(13); count = 0; }

                Console.WriteLine($"{i} -> {sketch.GetPinValue(13)}");
                Assert.IsTrue(count < 3, "D13 should blink (iteration: {0})", i);
            }
        }

        [TestMethod]
        public void Test098criptWith255Instructions()
        {
            LoadProgram(ReadFile(nameof(Test098criptWith255Instructions)));

            int last = sketch.GetPinValue(13);
            int count = 0;
            for (int i = 0; i < 1000; i++)
            {
                IncrementMillisAndExec(100, 1);

                if (sketch.GetPinValue(13) == last) { count++; }
                else { last = sketch.GetPinValue(13); count = 0; }

                Console.WriteLine($"{i} -> {sketch.GetPinValue(13)}");
                Assert.IsTrue(count < 3, "D13 should blink (iteration: {0})", i);
            }
        }



        [TestMethod]
        public void Test099CallingAScriptWithLessArgumentsThanRequired()
        {
            LoadProgram(ReadFile(nameof(Test099CallingAScriptWithLessArgumentsThanRequired)));

            sketch.SetMillis(0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test100CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test100CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test101CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test101CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test102CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test102CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test103CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test103CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test104CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test104CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test105CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test105CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test106CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test106CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test107CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test107CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test108CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test108CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test109CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test109CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }


        [TestMethod]
        public void Test110CompilingJumpsLongerThan7bitTwosComplement()
        {
            LoadProgram(ReadFile(nameof(Test110CompilingJumpsLongerThan7bitTwosComplement)));

            sketch.SetMillis(0);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(1000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(2000);
            Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));

            sketch.SetMillis(3000);
            Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }
    }
}