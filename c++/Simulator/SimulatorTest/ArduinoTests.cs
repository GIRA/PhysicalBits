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

        private static readonly string TEST_FILES_PATH = Path.Combine("..", "SimulatorTest", "ArduinoTestFiles");
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
        public void Test001Toggle8()
        {
            byte[] program = ReadFile(nameof(Test001Toggle8));
            TestProgram(program, 6100, new byte[] { 8 }, 10);
        }
        [TestMethod]
        public void Test002Toggle9()
        {
            byte[] program = ReadFile(nameof(Test002Toggle9));
            TestProgram(program, 6100, new byte[] { 9 }, 10);
        }
        [TestMethod]
        public void Test003Toggle10()
        {
            byte[] program = ReadFile(nameof(Test003Toggle10));
            TestProgram(program, 6100, new byte[] { 10 }, 10);
        }
        [TestMethod]
        public void Test004Toggle11()
        {
            byte[] program = ReadFile(nameof(Test004Toggle11));
            TestProgram(program, 6100, new byte[] { 11 }, 10);
        }
        [TestMethod]
        public void Test005Toggle12()
        {
            byte[] program = ReadFile(nameof(Test005Toggle12));
            TestProgram(program, 6100, new byte[] { 12 }, 10);
        }
        [TestMethod]
        public void Test006Toggle2000ms()
        {
            byte[] program = ReadFile(nameof(Test006Toggle2000ms));
            TestProgram(program, 10100, new byte[] { 11 }, 10);
        }
        [TestMethod]
        public void Test007Toggle500ms()
        {
            byte[] program = ReadFile(nameof(Test007Toggle500ms));
            TestProgram(program, 4100, new byte[] { 11 }, 10);
        }
        [TestMethod]
        public void Test008Toggle10ms()
        {
            byte[] program = ReadFile(nameof(Test008Toggle10ms));
            TestProgram(program, 4109, new byte[] { 11 }, 10);
        }

        [TestMethod]
        public void Test009PrimitiveYieldTime()
        {
            byte[] program = ReadFile(nameof(Test009PrimitiveYieldTime));
            TestProgram(program, 7099, new byte[] { 11 }, 10);

        }
        [TestMethod]
        public void Test010ResumeOnAPausedTaskShouldContinueFromItsCurrentPC()
        {
            byte[] program = ReadFile(nameof(Test010ResumeOnAPausedTaskShouldContinueFromItsCurrentPC));
            TestProgram(program, 7099, new byte[] { 11 }, 10);

        }
        [TestMethod]
        public void Test011ForLoopShouldOnlyEvaluateStepOncePerIteration()
        {
            byte[] program = ReadFile(nameof(Test011ForLoopShouldOnlyEvaluateStepOncePerIteration));
            TestProgram(program, 100, new byte[] { 11 }, 10);
        }

        [TestMethod]
        public void Test012CaptureEveryPinWithDifferentTickrate()
        {

            byte[] program = ReadFile(nameof(Test012CaptureEveryPinWithDifferentTickrate));
            TestProgram(program, 6909, new byte[] { 8, 9, 10, 11, 12 }, 10);
        }

    }
}
