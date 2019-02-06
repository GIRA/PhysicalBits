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


        static TestBench bench;

        [ClassInitialize]
        public static void ClassInit(TestContext context)
        {
            bench = new TestBench("COM3", "COM5");
        }

        [ClassCleanup]
        public static void TearDown()
        {
            bench.Dispose();
        }
        private void TestProgram(byte[] program, int Time, int errorTolerance)
        {
            var simulation = SketchRecorder.RecordExecution(program, Time).ToList();

            var execution = bench.RecordExecution(simulation.Select(s => s.ms), program).ToList();

            string sim = string.Join("\n", simulation.Select(e => e.ToString()));

            string ex = string.Join("\n", execution.Select(e => e.ToString()));

            int dif = simulation.Zip(execution, (s, c) => s.ms != c.ms || s.IsDifferentThan(c)).Count(b => b);
            double perc = (dif * 100.0) / simulation.Count;
            Console.WriteLine("there were {0} differences in {1} samples, giving an error of {2}%", dif, simulation.Count(), perc);
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine();

            Console.WriteLine("Simulated run:");
            Console.WriteLine(sim);
            Console.WriteLine();
            Console.WriteLine();
            Console.WriteLine("Captured run:");
            Console.WriteLine(ex);
            Assert.IsTrue(perc < errorTolerance, "There were more than {0}% of errors", errorTolerance);
        }



        [TestMethod]
        public void Test500msToggle()
        {
            //task n()
            //{
            //    write(D8, 0);
            //    write(D9, 0);
            //    write(D10, 0);
            //    write(D11, 0);
            //    write(D12, 0);
            //}
            //task blink11() running 2 / s {
            //    toggle(D11);
            //}
            //task blink13() running 1000 / s {
            //    toggle(D13);
            //}
            byte[] program = {  0, 3, 7, 24, 8, 9, 10, 11, 12, 13, 5, 1, 244, 128, 16, 131, 128, 161, 132, 128, 161, 133, 128, 161, 134, 128, 161, 135, 128, 161, 224, 192, 9, 2, 134, 162, 192, 1, 2, 136, 162 };
            TestProgram(program, 4000, 10);
        }

        [TestMethod]
        public void Test2000msToggle()
        {
            //task n()
            //{
            //    write(D8,0);
            //    write(D9, 0);
            //    write(D10, 0);
            //    write(D11, 0);
            //    write(D12, 0);
            //}
            //task blink11() running 0.5 / s {
            //    toggle(D11);
            //}
            //task blink13() running 1000 / s {
            //    toggle(D13);
            //}
            byte[] program = { 0, 3, 7, 24, 8, 9, 10, 11, 12, 13, 7, 68, 250, 0, 0, 128, 16, 131, 128, 161, 132, 128, 161, 133, 128, 161, 134, 128, 161, 135, 128, 161, 224, 192, 9, 2, 134, 162, 192, 1, 2, 136, 162 };
            TestProgram(program, 4000, 10);
        }


    }
}
