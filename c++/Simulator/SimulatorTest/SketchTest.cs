using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Simulator;
using System.IO;
using System.Linq;

namespace SimulatorTest
{
    [TestClass]
    public class SketchTest
    {
        private static readonly Sketch sketch = Sketch.Current;

        private const byte RQ_CONNECTION_REQUEST = 255;
        private const byte MAJOR_VERSION = 0;
        private const byte MINOR_VERSION = 6;

        [TestInitialize]
        public void Setup()
        {
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
            // INFO(Richo): Make sure we get disconnected before the next test.
            sketch.SetMillis(int.MaxValue);
            for (int i = 0; i < 1000; i++)
            {
                sketch.Loop();
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
            var str = File.ReadAllText(Path.Combine("TestFiles", fileName));
            return str.Split(new[] { ',', ' ' }, StringSplitOptions.RemoveEmptyEntries)
                .Select(each => byte.Parse(each))
                .ToArray();
        }

        [TestMethod]
        public void Test001TurnOnBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test001TurnOnBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test002TurnOffBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test002TurnOffBytecode)));
            sketch.SetPinValue(13, 1023);
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test003ReadWriteBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test003ReadWriteBytecode)));
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
            sketch.WriteSerial(ReadFile(nameof(Test004PushBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test005PushWithFloatingPointVariable()
        {
            sketch.WriteSerial(ReadFile(nameof(Test005PushWithFloatingPointVariable)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(Math.Round(0.2 * 1023), sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test006PopBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test006PopBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // The first loop sets the var to 1
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // And now we set the pin
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test007PrimBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test007PrimBytecode)));
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test008JZBytecode()
        {
            sketch.WriteSerial(ReadFile(nameof(Test008JZBytecode)));
            sketch.SetPinValue(13, 0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test009TickingRate()
        {
            sketch.WriteSerial(ReadFile(nameof(Test009TickingRate)));

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
            sketch.WriteSerial(ReadFile(nameof(Test010MultipleScriptsWithDifferentTickingRates)));

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
            sketch.WriteSerial(ReadFile(nameof(Test011YieldInstruction)));

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
            sketch.WriteSerial(ReadFile(nameof(Test012YieldInstructionPreservesStack)));

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
            sketch.WriteSerial(ReadFile(nameof(Test013YieldInstructionResumesOnNextTick)));

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
            sketch.WriteSerial(ReadFile(nameof(Test014PrimitiveYieldTime)));

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

            sketch.SetMillis(1199);
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));

            sketch.SetMillis(1200);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void Test015YieldAfterBackwardsJump()
        {
            sketch.WriteSerial(ReadFile(nameof(Test015YieldAfterBackwardsJump)));

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
            sketch.WriteSerial(ReadFile(nameof(Test016ScriptCallWithoutParametersOrReturnValue)));

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
            sketch.WriteSerial(ReadFile(nameof(Test017ScriptCallWithoutParametersWithReturnValueAndExplicitReturn)));

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
            sketch.WriteSerial(ReadFile(nameof(Test018ScriptTickingWithExplicitReturn)));

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
            sketch.WriteSerial(ReadFile(nameof(Test019ScriptWithYieldBeforeEndOfScript)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 100 + 50);
                sketch.Loop();
                sketch.SetMillis(i * 100 + 51);
                sketch.Loop();

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (iteration: {0})", i, msg);

                sketch.SetMillis(i * 100 + 75);
                sketch.Loop();
                sketch.SetMillis(i * 100 + 76);
                sketch.Loop();
                Assert.AreEqual(value, sketch.GetPinValue(11), "D11 should be {1} (iteration: {0})", i, msg);
            }
        }

        [TestMethod]
        public void Test020ScriptCallWithOneParameterAndReturnValue()
        {
            sketch.WriteSerial(ReadFile(nameof(Test020ScriptCallWithOneParameterAndReturnValue)));

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
            sketch.WriteSerial(ReadFile(nameof(Test021ScriptCallWithOneParameterWithoutReturnValue)));

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
            sketch.WriteSerial(ReadFile(nameof(Test022ScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn)));

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
            sketch.WriteSerial(ReadFile(nameof(Test023ScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn)));

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
            sketch.WriteSerial(ReadFile(nameof(Test024ScriptCallWithTwoParametersWithReturnValue)));

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
            sketch.WriteSerial(ReadFile(nameof(Test025ScriptCallWithRecursiveCall4LevelsDeep)));

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
            sketch.WriteSerial(ReadFile(nameof(Test026ScriptTickingThatAlsoCallsItself)));

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
            sketch.WriteSerial(ReadFile(nameof(Test027PrimitiveCoroutineShouldReturnTheIndexOfTheActiveScript)));

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
            sketch.WriteSerial(ReadFile(nameof(Test028PrimitiveBitwiseAnd)));

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
            sketch.WriteSerial(ReadFile(nameof(Test029PrimitiveBitwiseOr)));

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
            sketch.WriteSerial(ReadFile(nameof(Test030PrimitiveLogicalAnd)));

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
            sketch.WriteSerial(ReadFile(nameof(Test031PrimitiveLogicalOr)));

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
            sketch.WriteSerial(ReadFile(nameof(Test032StopScriptAndRestartShouldResetPCAndStuff)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off");

                sketch.SetMillis(i * 1000 + 550);
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
                Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should always be off");
            }
        }

        [TestMethod]
        public void Test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart()
        {
            sketch.WriteSerial(ReadFile(nameof(Test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart)));

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
            sketch.WriteSerial(ReadFile(nameof(Test034StartOnTheCurrentTaskShouldJumpToTheBeginning)));

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
            sketch.WriteSerial(ReadFile(nameof(Test035StartOnAnotherTaskShouldResetToBeginning)));

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
            sketch.WriteSerial(ReadFile(nameof(Test036ResumeOnARunningTaskShouldHaveNoEffect)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 100 + 50);
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC()
        {
            sketch.WriteSerial(ReadFile(nameof(Test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023 * (1 - (i % 2)), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test038ResumeOnStoppedTaskShouldJumpToBeginning()
        {
            sketch.WriteSerial(ReadFile(nameof(Test038ResumeOnStoppedTaskShouldJumpToBeginning)));

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
            sketch.WriteSerial(ReadFile(nameof(Test039StartOnStoppedTaskShouldJumpToBeginning)));

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
            sketch.WriteSerial(ReadFile(nameof(Test040StartOnPausedTaskShouldJumpToBeginning)));

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
            sketch.WriteSerial(ReadFile(nameof(Test041PausingShouldPreserveTheStack)));

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
            sketch.WriteSerial(ReadFile(nameof(Test042EmptyScriptShouldNotCrashTheVM)));
            sketch.Loop();
        }

        [TestMethod]
        public void Test043ForLoop()
        {
            sketch.WriteSerial(ReadFile(nameof(Test043ForLoop)));

            for (int i = 7; i <= 11; i++)
            {
                sketch.SetMillis(i);
                sketch.Loop();

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
            sketch.WriteSerial(ReadFile(nameof(Test044ReversedForLoop)));

            for (int i = 11; i >= 7; i--)
            {
                sketch.SetMillis(100 - i);
                sketch.Loop();

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
            sketch.WriteSerial(ReadFile(nameof(Test045ForLoopWithoutConstantStep)));

            for (int i = 7; i <= 11; i++)
            {
                sketch.SetMillis(i);
                sketch.Loop();

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
            sketch.WriteSerial(ReadFile(nameof(Test046ReverseForLoopWithoutConstantStep)));

            for (int i = 11; i >= 7; i--)
            {
                sketch.SetMillis(100 - i);
                sketch.Loop();

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
            sketch.WriteSerial(ReadFile(nameof(Test047ForLoopShouldOnlyEvaluateStepOncePerIteration)));

            for (int i = 0; i < 100; i++)
            {
                sketch.SetMillis(i * 1000 + 50);
                sketch.Loop();
                Assert.AreEqual(1023 * (i % 2), sketch.GetPinValue(13), "D13 should blink on each tick");
            }
        }

        [TestMethod]
        public void Test048MutexShouldGuaranteeACriticalSection()
        {
            sketch.WriteSerial(ReadFile(nameof(Test048MutexShouldGuaranteeACriticalSection)));
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
            double[] pattern = new [] { 0, 0.25, 0.5, 0.75, 1, 1, 1, 0.5, 0.5, 0, 0, 0 };
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
            sketch.WriteSerial(ReadFile(nameof(Test049ChannelShouldDeadlockIfConsumingFromTheSameTaskAsProducer)));
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
            sketch.WriteSerial(ReadFile(nameof(Test050ChannelWithMultipleProducersAndNoConsumerShouldBlockAllProducers)));
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
            sketch.WriteSerial(ReadFile(nameof(Test051ChannelWithOneProducerAndOneConsumerBlocksTheProducerAtTheRateOfConsumer)));
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
            sketch.WriteSerial(ReadFile(nameof(Test052ChannelWithMultipleProducersAndOneConsumer)));
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
            sketch.WriteSerial(ReadFile(nameof(Test053ChannelWithMultipleConsumersAndOneProducer)));
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
            sketch.WriteSerial(ReadFile(nameof(Test054VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther)));

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
            sketch.WriteSerial(ReadFile(nameof(Test055VariablesWithTheSameNameInDifferentScopesShouldNotInterfereWithEachOther)));

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
            sketch.WriteSerial(ReadFile(nameof(Test056Round)));
            
            sketch.SetMillis(1000);
            sketch.Loop();
            
            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should be off");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should be on");
        }

        [TestMethod]
        public void Test057Ceil()
        {
            sketch.WriteSerial(ReadFile(nameof(Test057Ceil)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(10), "D10 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(11), "D11 should be on");
            Assert.AreEqual(1023, sketch.GetPinValue(12), "D12 should be on");
        }

        [TestMethod]
        public void Test058Floor()
        {
            sketch.WriteSerial(ReadFile(nameof(Test058Floor)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(10), "D10 should be off");
            Assert.AreEqual(0, sketch.GetPinValue(11), "D11 should be off");
            Assert.AreEqual(0, sketch.GetPinValue(12), "D12 should be off");
        }

        [TestMethod]
        public void Test059Sqrt()
        {
            sketch.WriteSerial(ReadFile(nameof(Test059Sqrt)));

            sketch.SetMillis(1000);
            sketch.Loop();
            
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(9) - 512) < 5, "D9 should be close to 512");
        }

        [TestMethod]
        public void Test060Abs()
        {
            sketch.WriteSerial(ReadFile(nameof(Test060Abs)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(13), "D13 should be on");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(9) - 512) < 5, "D9 should be close to 512");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(10) - 512) < 5, "D10 should be close to 512");
        }

        [TestMethod]
        public void Test061NaturalLogarithm()
        {
            sketch.WriteSerial(ReadFile(nameof(Test061NaturalLogarithm)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test062Log10()
        {
            sketch.WriteSerial(ReadFile(nameof(Test062Log10)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test063Exp()
        {
            sketch.WriteSerial(ReadFile(nameof(Test063Exp)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test064Pow10()
        {
            sketch.WriteSerial(ReadFile(nameof(Test064Pow10)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test065IsCloseTo()
        {
            sketch.WriteSerial(ReadFile(nameof(Test065IsCloseTo)));

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
            sketch.WriteSerial(ReadFile(nameof(Test066Asin)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test067Acos()
        {
            sketch.WriteSerial(ReadFile(nameof(Test067Acos)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test068Atan()
        {
            sketch.WriteSerial(ReadFile(nameof(Test068Atan)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test069Power()
        {
            sketch.WriteSerial(ReadFile(nameof(Test069Power)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test070IsOn()
        {
            sketch.WriteSerial(ReadFile(nameof(Test070IsOn)));

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
            sketch.WriteSerial(ReadFile(nameof(Test071IsOff)));

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
            sketch.WriteSerial(ReadFile(nameof(Test072Mod)));

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
            sketch.WriteSerial(ReadFile(nameof(Test073Constrain)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(1023, sketch.GetPinValue(7), "D7 should be on");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test074RandomInt()
        {
            sketch.WriteSerial(ReadFile(nameof(Test074RandomInt)));

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
            sketch.WriteSerial(ReadFile(nameof(Test075Random)));

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
            sketch.WriteSerial(ReadFile(nameof(Test076IsEven)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test077IsOdd()
        {
            sketch.WriteSerial(ReadFile(nameof(Test077IsOdd)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test078IsPrime()
        {
            sketch.WriteSerial(ReadFile(nameof(Test078IsPrime)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test079IsWhole()
        {
            sketch.WriteSerial(ReadFile(nameof(Test079IsWhole)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test080IsPositive()
        {
            sketch.WriteSerial(ReadFile(nameof(Test080IsPositive)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test081IsNegative()
        {
            sketch.WriteSerial(ReadFile(nameof(Test081IsNegative)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }

        [TestMethod]
        public void Test082IsDivisibleBy()
        {
            sketch.WriteSerial(ReadFile(nameof(Test082IsDivisibleBy)));

            sketch.SetMillis(1000);
            sketch.Loop();

            Assert.AreEqual(0, sketch.GetPinValue(7), "D7 should be off");
            Assert.IsTrue(Math.Abs(sketch.GetPinValue(8) - 512) < 5, "D8 should be close to 512");
            Assert.AreEqual(1023, sketch.GetPinValue(9), "D9 should be on");
        }
        
        [TestMethod]
        public void Test083DelayS()
        {
            sketch.WriteSerial(ReadFile(nameof(Test083DelayS)));

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
            sketch.WriteSerial(ReadFile(nameof(Test084DelayM)));

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
            sketch.WriteSerial(ReadFile(nameof(Test085Minutes)));

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
            sketch.WriteSerial(ReadFile(nameof(Test086Seconds)));

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
            sketch.WriteSerial(ReadFile(nameof(Test087Millis)));

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
            sketch.WriteSerial(ReadFile(nameof(Test088ScriptCallOverridingPrimitive)));
            
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
    }
}
