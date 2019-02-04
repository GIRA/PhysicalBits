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
            var execution = SketchRecorder.RecordExecution(ReadFile(nameof(Test009TickingRate)), 4000).ToList();

        }

    }
}
