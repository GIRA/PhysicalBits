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

        public IEnumerable<ExecutionSnapshot> RecordExecution(byte[] program, int targetTime,IEnumerable<byte> pins)
        {
            //send an empty program
            uzi.runProgram(emptyProgram);
            System.Threading.Thread.Sleep(100);
            //wait for the mega to be ready   
            string line = "";
            while (line != "Ready. Waiting for target time\r")
            {
                line = mega.ReadLine();
            }
            byte pinFlag = getPinFlag(pins);
            mega.Write(targetTime.ToString()+","+pinFlag.ToString());

            System.Threading.Thread.Sleep(100);
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
                byte pinData = byte.Parse(parts[1]);
                for (int j = 0; j < current.pins.Length; j++)
                {
                    current.pins[j] = (byte)(pinData & 1) ;
                    pinData = (byte)(pinData >> 1);
                } 
                result.Add(current);
                line = mega.ReadLine();
            }
            return result;
        }

        private byte getPinFlag(IEnumerable<byte> pins)
        {
            //INFO(Tera): Assuming the wiring is according to the convention, at least until a more permanent solution is built,
            //the result of this function is a bitflag composed of the following pins:
            //? ? ? 12 11 10 9 8 
            byte result = 0;
            foreach (var item in pins)
            {
                switch (item)
                {
                    case 12:
                        result |= 0b00010000;
                        break;
                    case 11:
                        result |= 0b00001000;
                        break;

                    case 10:
                        result |= 0b00000100;
                        break;
                    case 9:
                        result |= 0b00000010;
                        break;

                    case 8:
                        result |= 0b00000001;
                        break;
                    default:
                        Console.WriteLine("Invalid Operation, pin {0} requested to the arduino recorder", item);
                        break;
                }

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
