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
        private Sketch sketch = Sketch.Current;

        [TestInitialize]
        public void Setup()
        {
            sketch.SetMillis(-1);
            sketch.Setup();
            TurnOffAllPins();
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
    }
}
