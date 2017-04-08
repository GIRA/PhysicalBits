using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Simulator;

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

        [TestMethod]
        public void TestTurnOnBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi
                    program: [:p | p
	                    script: #test
	                    ticking: true
	                    delay: 0
	                    bytecodes: [:s | s
		                    turnOn: 13]].
                UziProtocol new run: program
                */
                0, 1, 0, 128, 0, 0, 0, 1, 13
            });
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestTurnOffBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi 
                    program: [:p | p
	                    script: #test
	                    ticking: true
	                    delay: 0
	                    bytecodes: [:s | s
		                    turnOff: 13]].
                UziProtocol new run: program
                */
                0, 1, 0, 128, 0, 0, 0, 1, 45
            });
            sketch.SetPinValue(13, 1023);
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestReadWriteBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi 
                    program: [:p | p
	                    script: #test
	                    ticking: true
	                    delay: 0
	                    bytecodes: [:s | s
		                    read: 15;
		                    write: 13]].
                UziProtocol new run: program
                */
                0, 1, 0, 128, 0, 0, 0, 2, 111, 77
            });
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
        public void TestPushBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 0
	                bytecodes: [:s | s
		                push: 1;		
		                write: 13]].
                UziProtocol new run: program.
                */
                0, 1, 1, 4, 1, 128, 0, 0, 0, 2, 128, 77
            });
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestPushWithFloatingPointVariable()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 0
	                bytecodes: [:s | s
		                push: 0.2;		
		                write: 13]].
                UziProtocol new run: program.
                */
                0, 1, 1, 7, 62, 76, 204, 205, 128, 0, 0, 0, 2, 128, 77
            });
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(Math.Round(0.2 * 1023), sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestPopBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true 
	                delay: 0
	                bytecodes: [:s | s
		                push: #a;
		                write: 13;
		                push: 1;
		                pop: #a]].
                UziProtocol new run: program.
                */
                0, 1, 2, 8, 0, 1, 128, 0, 0, 0, 4, 128, 77, 129, 144
            });
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // The first loop sets the var to 1
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop(); // And now we set the pin
            Assert.AreEqual(1023, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestPrimBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true 
	                delay: 0
	                bytecodes: [:s | s
		                push: 13;
		                prim: #toggle]].
                UziProtocol new run: program.
                */
                0, 1, 1, 4, 13, 128, 0, 0, 0, 2, 128, 162
            });
            Assert.AreEqual(0, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestJZBytecode()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true 
	                delay: 0
	                instructions: [:s | s
		                read: 13;
		                jz: #zero;
		                turnOff: 13;
		                jmp: #end;
		                label: #zero;
		                turnOn: 13;
		                label: #end]].
                UziProtocol new run: program.
                */
                0, 1, 0, 128, 0, 0, 0, 5, 109, 241, 2, 45, 240, 1, 13
            });
            sketch.SetPinValue(13, 0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        [TestMethod]
        public void TestTickingRate()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true 
	                delay: 1000
	                instructions: [:s | s
		                push: 13;
		                prim: #toggle]].
                UziProtocol new run: program.
                */
                0, 1, 1, 4, 13, 128, 0, 3, 232, 2, 128, 162
            });

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
        public void TestMultipleScriptsWithDifferentTickingRates()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true 
	                delay: 1000
	                instructions: [:s | s
		                push: 13;
		                prim: #toggle];
	                script: #pot
	                ticking: true
	                delay: 100
	                instructions: [:s | s
		                read: 15;
		                write: 9]].
                UziProtocol new run: program.
                */
                0, 2, 1, 4, 13, 128, 0, 3, 232, 2, 128, 162, 128, 0, 0, 100, 2, 111, 73
            });

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
        public void TestYieldInstruction()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #yieldTest
	                ticking: true
	                delay: 0
	                instructions: [:s | s
		                turnOn: 13;
		                prim: #yield;
		                turnOff: 13]].
                UziProtocol new run: program
                */
                0, 1, 0, 128, 0, 0, 0, 3, 13, 182, 45
            });

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
        public void TestYieldInstructionPreservesStack()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #yieldTest
	                ticking: true
	                delay: 0
	                instructions: [:s | s
		                push: 13;
		                prim: #toggle;
		                push: 13;
		                prim: #yield;
		                prim: #toggle;
		                push: 12;
		                push: 1;
		                prim: #yield;
		                prim: #add;
		                prim: #toggle]].
                UziProtocol new run: program
                */
                0, 1, 3, 12, 1, 12, 13, 128, 0, 0, 0, 10, 130, 162,
                130, 169, 162, 129, 128, 169, 166, 162
            });

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
        public void TestYieldInstructionResumesOnNextTick()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #yieldTest
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                turnOn: 12;
		                prim: #yield;
		                turnOff: 12]].
                UziProtocol new run: program
                */
                0, 1, 0, 128, 0, 3, 232, 3, 12, 182, 44
            });

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
        public void TestPrimitiveYieldTime()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #yieldTest
	                ticking: true
	                delay: 100
	                instructions: [:s | s
		                turnOn: 13;
		                push: 1000;
		                prim: #yieldTime;
		                turnOff: 13]].
                UziProtocol new run: program
                */
                0, 1, 1, 5, 3, 232, 128, 0, 0, 100, 4, 13, 128, 183, 45
            });

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
        public void TestYieldAfterBackwardsJump()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink 
	                ticking: true 
	                delay: 1000
	                instructions: [:s | s push: 11; prim: #toggle];
	                script: #main
	                ticking: true
	                delay: 0
	                instructions: [:s | s
		                push: 13; prim: #toggle;
		                label: #label1; read: 15; jz: #label1;
		                label: #label2; read: 15; jnz: #label2]].
                UziProtocol new run: program
                */
                0, 2, 2, 8, 11, 13, 128, 0, 3, 232, 2, 128, 162, 128, 0,
                0, 0, 6, 129, 162, 111, 241, 254, 111, 242, 254
            });

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
        public void TestScriptCallWithoutParametersOrReturnValue()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #toggle
	                ticking: false
	                delay: 0
	                instructions: [:s | s
		                push: 10;
		                prim: #toggle;
		                prim: #ret;
		                turnOn: 13];
	                script: #loop
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #toggle;
                        prim: #pop;
		                push: 11;
		                prim: #toggle]].
                UziProtocol new run: program.
                */
                0, 2, 2, 8, 10, 11, 0, 0, 0, 0, 4, 128, 162, 185, 13,
                128, 0, 3, 232, 4, 192, 186, 129, 162
            });

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
        public void TestScriptCallWithoutParametersWithReturnValueAndExplicitReturn()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                registerVariable: #counter value: 5;
	
	                script: #incr
	                ticking: false
	                delay: 0
	                instructions: [:s | s
		                push: #counter;
		                push: 1;
		                prim: #add;
		                pop: #counter;
		                push: #counter;
		                writeLocal: 0;
		                prim: #ret;
		                push: 13;
		                prim: #toggle];
	
	                script: #loop
	                ticking: true
	                delay: 100
	                instructions: [:s | s
		                call: #incr;
		                push: 100;
		                prim: #divide;
		                write: 11]].
                UziProtocol new run: program.
                */
                0, 2, 4, 16, 1, 5, 13, 100, 0, 0, 0, 0, 9, 129, 128, 166, 145, 129, 255,
                128, 185, 130, 162, 128, 0, 0, 100, 4, 192, 131, 167, 75
            });

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
        public void TestScriptCallWithoutParametersWithReturnValueAndImplicitReturn()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                registerVariable: #counter value: 5;
	
	                script: #incr
	                ticking: false
	                delay: 0
	                instructions: [:s | s
		                push: #counter;
		                push: 1;
		                prim: #add;
		                pop: #counter;
		                push: #counter;
		                writeLocal: 0;
		                push: 13;
		                prim: #toggle];
	
	                script: #loop
	                ticking: true
	                delay: 100
	                instructions: [:s | s
		                call: #incr;
		                push: 100;
		                prim: #divide;
		                write: 11]].
                UziProtocol new run: program.
                */
                0, 2, 4, 16, 1, 5, 13, 100, 0, 0, 0, 0, 8, 129, 128, 166, 145, 129, 255,
                128, 130, 162, 128, 0, 0, 100, 4, 192, 131, 167, 75
            });

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

                bool on = i % 2 == 0;
                int value = on ? 1023 : 0;
                string msg = on ? "on" : "off";
                Assert.AreEqual(value, sketch.GetPinValue(13), "D13 should be {1} (iteration: {0})", i, msg);

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
        public void TestScriptTickingWithExplicitReturn()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p	
	                script: #incr
	                ticking: true
	                delay: 1
	                instructions: [:s | s
					    push: 13;
					    prim: #toggle;
		                push: #counter;
		                push: 1;
		                prim: #add;
		                pop: #counter;
		                push: #counter;
		                writeLocal: 0;
		                prim: #ret;
		                push: 11;
		                prim: #toggle]].
                UziProtocol new run: program
                */
                0, 1, 4, 16, 0, 1, 11, 13, 128, 0, 0, 1, 11, 131, 162, 128,
                129, 166, 144, 128, 255, 128, 185, 130, 162
            });

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
        public void TestScriptWithYieldBeforeEndOfScript()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p	
	                script: #blink11
	                ticking: false
	                delay: 0
	                instructions: [:s | s
					    push: 11;
					    prim: #toggle];
				    script: #main
				    ticking: true
				    delay: 0
				    instructions: [:s | s
					    call: #blink11;
					    prim: #pop;
					    push: 100;
					    prim: #yieldTime]].
                UziProtocol new run: program
                */
                0, 2, 2, 8, 11, 100, 0, 0, 0, 0, 2, 128, 162, 128,
                0, 0, 0, 4, 192, 186, 129, 183
            });

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
        public void TestScriptCallWithOneParameterAndReturnValue()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #main;
		                prim: #pop];
	
	                script: #toggle
	                ticking: false
	                delay: 0
	                args: #(n)
	                instructions: [:s | s
		                push: 1;
		                readLocal: #n;
		                prim: #subtract;
		                prim: #retv;
		                push: 11;
		                prim: #toggle];
	
	                script: #main 
	                ticking: false 
	                delay: 10000000
	                instructions: [:s | s
		                read: 13;
		                call: #toggle;
		                write: 13]].
                UziProtocol new run: program.
                */
                0, 3, 2, 8, 1, 11, 128, 0, 3, 232, 2, 194, 186, 64, 0, 0, 0, 1, 6, 128,
                255, 0, 168, 187, 129, 162, 0, 152, 150, 128, 3 ,109, 193, 77
            });
            
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
        public void TestScriptCallWithOneParameterWithoutReturnValue()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #main;
		                prim: #pop];
	
	                script: #toggle
	                ticking: false
	                delay: 0
	                args: #(n)
	                instructions: [:s | s
		                push: 1;
		                readLocal: #n;
		                prim: #subtract;
		                write: 13];
	
	                script: #main 
	                ticking: false 
	                delay: 10000000
	                instructions: [:s | s
		                read: 13;
		                call: #toggle;
		                prim: #pop]].
                UziProtocol new run: program.
                */
                0, 3, 1, 4, 1, 128, 0, 3, 232, 2, 194, 186, 64, 0, 0, 0, 1, 4, 128, 255,
                0, 168, 77, 0, 152, 150, 128, 3, 109, 193, 186
            });

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
        public void TestScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #main;
		                prim: #pop];
	
	                script: #toggle
	                ticking: false
	                delay: 0
	                args: #(n)
	                instructions: [:s | s
		                push: 1;
		                readLocal: #n;
		                prim: #subtract;
		                write: 13;
		                prim: #ret;
		                turnOn: 11];
	
	                script: #main 
	                ticking: false 
	                delay: 10000000
	                instructions: [:s | s
		                read: 13;
		                call: #toggle;
		                write: 11]].
                UziProtocol new run: program.
                */
                0, 3, 1, 4, 1, 128, 0, 3, 232, 2, 194, 186, 64, 0, 0, 0, 1, 6, 128, 255,
                0, 168, 77, 185, 11, 0, 152, 150, 128, 3, 109, 193, 75
            });

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
        public void TestScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #main;
		                prim: #pop];
	
	                script: #toggle
	                ticking: false
	                delay: 0
	                args: #(a b)
	                instructions: [:s | s
		                readLocal: #a;
		                readLocal: #b;
		                prim: #subtract;
		                write: 13;
		                prim: #ret;
		                turnOn: 11];
	
	                script: #main 
	                ticking: false 
	                delay: 10000000
	                instructions: [:s | s
		                push: 1;
		                read: 13;
		                call: #toggle;
		                write: 11]].
                UziProtocol new run: program.
                */
                0, 3, 1, 4, 1, 128, 0, 3, 232, 2, 194, 186, 64, 0, 0, 0, 2, 6, 255, 0, 255, 1,
                168, 77, 185, 11, 0, 152, 150, 128, 4, 128, 109, 193, 75
            });

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
        public void TestScriptCallWithTwoParametersWithReturnValue()
        {
            sketch.WriteSerial(new byte[]
            {
                /*
                program := Uzi program: [:p | p
	                script: #blink
	                ticking: true
	                delay: 1000
	                instructions: [:s | s
		                call: #main;
		                prim: #pop];
	
	                script: #toggle
	                ticking: false
	                delay: 0
	                args: #(a b)
	                instructions: [:s | s
		                readLocal: #a;
		                readLocal: #a;
		                readLocal: #b;
		                prim: #subtract;
		                prim: #multiply;
		                prim: #retv;
		                push: 11;
		                prim: #toggle];
	
	                script: #main 
	                ticking: false 
	                delay: 10000000
	                instructions: [:s | s
		                push: 1;
		                read: 13;
		                call: #toggle;
		                write: 13]].
                UziProtocol new run: program.
                */
                0, 3, 2, 8, 1, 11, 128, 0, 3, 232, 2, 194, 186, 64, 0, 0, 0, 2, 8, 255, 0, 255,
                0, 255, 1, 168, 165, 187, 129, 162, 0, 152, 150, 128, 4, 128, 109, 193, 77
            });

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
    }
}
