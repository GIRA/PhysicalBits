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
        private double calculateSlope(IEnumerable<int> times, IEnumerable<int> deviations)
        {
            var sumX = times.Sum();
            var sumX2 = times.Sum(x => (long)x * (long)x);
            var sumY = (double)deviations.Sum();
            var sumXY = times.Zip(deviations, (x, y) => (long)x * y).Sum();
            var n = (double)times.Count();

            //b = (sum(x*y) - sum(x)sum(y)/n)
            //      / (sum(x^2) - sum(x)^2/n)
            return (sumXY - ((sumX * sumY) / n))
                        / (sumX2 - (sumX * sumX / n));
        }
        private void TestProgram(byte[] program, int Time, IEnumerable<byte> interestingPints, int errorTolerance)
        {
            var simulation = SketchRecorder.RecordExecution(program, Time, interestingPints).ToList();

            var execution = bench.RecordExecution(program, Time, interestingPints).ToList();

            string sim = string.Join("\n", simulation.Select(e => e.ToString()));

            string ex = string.Join("\n", execution.Select(e => e.ToString()));
            //the pin of the snapshots should be the same.
            int dif = simulation.Zip(execution, (s, c) => s.IsDifferentThan(c)).Count(b => b);
            double perc = (dif * 100.0) / simulation.Count;

            var msDif = simulation.Zip(execution, (s, c) => c.ms - s.ms);
            var slope = calculateSlope(simulation.Select(s => s.ms), msDif);
            Console.WriteLine("Time difference between simulation and execution:");
            Console.WriteLine("Max: {0}. Min: {1}. Avg: {2}", msDif.Max(), msDif.Min(), msDif.Average());
            Console.WriteLine("There was a slope of {0}, deviating {1}ms every second", slope, slope * 1000);
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
        public void Test10msToggle()
        {
            //task blink11() running 100 / s {
            //    toggle(D11);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 12, 10, 11, 13, 192, 3, 2, 132, 162, 128, 2, 133, 162 };
            TestProgram(program, 4109, new byte[] { 11 }, 10);
        }

        [TestMethod]
        public void Test500msToggle()
        {
            //task blink11() running 2 / s {
            //    toggle(D11);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 11, 13, 5, 1, 244, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 4100, new byte[] { 11 }, 10);
        }

        [TestMethod]
        public void Test2000msToggle()
        {
            //task blink11() running 0.5 / s {
            //    toggle(D11);
            //}
            //task blink13() running {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 11, 13, 7, 68, 250, 0, 0, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 10100, new byte[] { 11 }, 10);
        }

        [TestMethod]
        public void TestToggle8()
        {

            //task blink() running 1 / s {
            //    toggle(D8);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 8, 13, 5, 3, 232, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 6100, new byte[] { 8 }, 10);
        }
        [TestMethod]
        public void TestToggle9()
        {
            //task blink() running 1.2 / s {
            //    toggle(D9);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 9, 13, 7, 68, 80, 85, 85, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 6100, new byte[] { 9 }, 10);
        }
        [TestMethod]
        public void TestToggle10()
        {

            //task blink() running 1.4 / s {
            //    toggle(D10);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 10, 13, 7, 68, 50, 146, 73, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 6100, new byte[] { 10 }, 10);
        }
        [TestMethod]
        public void TestToggle11()
        {

            //task blink() running 1.47 / s {
            //    toggle(D11);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 11, 13, 7, 68, 42, 17, 106, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 6100, new byte[] { 11 }, 10);
        }
        [TestMethod]
        public void TestToggle12()
        {
            //task blink() running 1.9 / s {
            //    toggle(D11);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 3, 8, 12, 13, 7, 68, 3, 148, 54, 192, 5, 2, 131, 162, 128, 2, 132, 162 };
            TestProgram(program, 6100, new byte[] { 12 }, 10);
        }


        [TestMethod]
        public void TestAllPinsWithDifferentTickrates()
        {

            //task blink8() running 1 / s {
            //    toggle(D8);
            //}
            //task blink9() running 1.2 / s {
            //    toggle(D9);
            //}
            //task blink110() running 1.3 / s {
            //    toggle(D10);
            //}
            //task blink11() running 1.5 / s {
            //    toggle(D11);
            //}
            //task blink12() running 1.7 / s {
            //    toggle(D12);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 6, 11, 24, 8, 9, 10, 11, 12, 13, 9, 1, 244, 3, 232, 15, 67, 166, 170, 171, 68, 250, 0, 0, 69, 80, 85, 85, 192, 11, 2, 131, 162, 192, 9, 2, 132, 162, 192, 10, 2, 133, 162, 192, 12, 2, 134, 162, 192, 13, 2, 135, 162, 128, 2, 136, 162 };
            TestProgram(program, 6909, new byte[] { 8, 9, 10, 11, 12 }, 10);
        }

        [TestMethod]
        public void TestPrimitiveYieldTime()
        {
            //task blink11() running 10 / s {
            //    turnOn(D11);
            //    delayMs(1000);
            //    turnOff(D11);
            //}
            //task blink13() running  {
            //    toggle(D13);
            //}
            byte[] program = { 0, 2, 4, 12, 11, 13, 100, 5, 3, 232, 192, 5, 6, 131, 180, 134, 183, 131, 181, 128, 2, 132, 162 };
            TestProgram(program, 7099, new byte[] { 11 }, 10);

        }
    }
}
