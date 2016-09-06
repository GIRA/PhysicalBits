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
            sketch.Setup();
            TurnOffAllPins();
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
                0, 1, 0, 128, 0, 0, 0, 5, 109, 241, 2, 45, 255, 1, 13
            });
            sketch.SetPinValue(13, 0);
            sketch.Loop();
            Assert.AreEqual(1023, sketch.GetPinValue(13));
            sketch.Loop();
            Assert.AreEqual(0, sketch.GetPinValue(13));
        }

        private void TurnOffAllPins()
        {
            for (int i = 0; i < 19; i++)
            {
                sketch.SetPinValue(i, 0);
            }
        }
    }
}
