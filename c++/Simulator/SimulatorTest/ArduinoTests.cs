using Microsoft.VisualStudio.TestTools.UnitTesting;
using Simulator;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SimulatorTest
{
    [TestClass]
    public class ArduinoTests
    {

        private static readonly string TEST_FILES_PATH = Path.Combine("..", "SimulatorTest", "TestFiles");
        private byte[] ReadFile(string fileName)
        {
            var str = File.ReadAllText(Path.Combine(TEST_FILES_PATH, fileName));
            return str.Split(new[] { ',', ' ' }, StringSplitOptions.RemoveEmptyEntries)
                .Select(each => byte.Parse(each))
                .ToArray();
        }


        [TestMethod]
        public void Test009TickingRate()
        {
            TestBench bench = new TestBench("COM3", "COM5");

            //task n()
            //{
            //    write(D9, 0);
            //    write(D10, 0);
            //    write(D11, 0);
            //    write(D12, 0);

            //}
            //task blink13() running 1000 / s {
            //    toggle(D13);
            //}

            //task blink11() running 2 / s {
            //    toggle(D11);
            //}

            byte[] program = { 0, 3, 6, 20, 9, 10, 11, 12, 13, 5, 1, 244, 128, 13, 131, 128, 161, 132, 128, 161, 133, 128, 161, 134, 128, 161, 224, 192, 1, 2, 135, 162, 192, 8, 2, 133, 162 };
            var simulation = SketchRecorder.RecordExecution(program, 4000).ToList();

            var execution= bench.RecordExecution(simulation.Select(s => s.ms), program);
            string sim= string.Join("\n", simulation.Select(e => e.ToString()));
            string ex= string.Join("\n", execution.Select(e => e.ToString()));
        }

    }
}
