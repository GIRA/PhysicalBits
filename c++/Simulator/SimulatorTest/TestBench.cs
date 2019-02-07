using Simulator;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SimulatorTest
{
    public class TestBench : IDisposable
    {
        private readonly int baudRate = 57600;

        private readonly byte[] emptyProgram = { 0, 0, 0 };

        private UziConnection uzi;
        private readonly string benchArduinoPort;

        private System.IO.Ports.SerialPort mega;

        public TestBench(string UziArduinoPort, string BenchArduinoPort)
        {
            uzi = new UziConnection(UziArduinoPort, baudRate);
            benchArduinoPort = BenchArduinoPort;
            uzi.Start();
            mega = new System.IO.Ports.SerialPort(BenchArduinoPort, baudRate);
            mega.Encoding = Encoding.ASCII;
            mega.Open();
        }

        public IEnumerable<ExecutionSnapshot> RecordExecution(byte[] program, int targetTime)
        {
            //send an empty program
            uzi.runProgram(emptyProgram);
            //wait for the mega to be ready   
            string line = "";
            while (line != "Ready. Waiting for target time\r")
            {
                line = mega.ReadLine();
            }

            mega.Write(targetTime.ToString());
            uzi.runProgram(program);

            while (line != "Finished Capture\r")
            {
                line = mega.ReadLine();
            }
            List<ExecutionSnapshot> result = new List<ExecutionSnapshot>();

            line = mega.ReadLine();
            while (line != "Ready. Waiting for target time\r")
            {
                var current = new ExecutionSnapshot();
                var parts = line.Split(',');
                current.ms = int.Parse(parts[0]);
                byte pins = byte.Parse(parts[1]);
                for (int j = 0; j < current.pins.Length; j++)
                {
                    current.pins[j] = (byte)((pins & 1 << (8 - j)) >> (8 - j));
                } 
                result.Add(current);
                line = mega.ReadLine();
            }
            return result;
        }

        public void Dispose()
        {
            mega.Dispose();
            uzi.Dispose();

        }
    }
}
