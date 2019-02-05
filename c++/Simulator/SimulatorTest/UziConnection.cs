using System;
using System.Collections.Generic;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace SimulatorTest
{
    public class UziConnection
    {
        private string uziArduinoPort;
        private int baudRate;
        private SerialPort port;


        public UziConnection(string uziArduinoPort, int baudRate)
        {
            this.uziArduinoPort = uziArduinoPort;
            this.baudRate = baudRate;


        }


        internal void Start()
        {
            port = new SerialPort(uziArduinoPort, baudRate);

        }


        private byte[] readAvailable()
        {
            var s = port.ReadExisting();
            return port.Encoding.GetBytes(s);
        }
        private byte readNext()
        {
            return (byte)port.ReadByte();
        }
        private void write(byte[] bytes)
        {
            port.Write(bytes, 0, bytes.Length);
        }
        //TODO(Tera): this definitions should be centralized, probably on the sketch
        private const byte RQ_CONNECTION_REQUEST = 255;
        private const byte MAJOR_VERSION = 0;
        private const byte MINOR_VERSION = 6;
        private const byte KEEP_ALIVE = 7;

        private void performHandshake(int retries = 3)
        {
            /*
          * INFO(Richo): Perform connection request and handshake.
          * Otherwise, when we send a program later we will be rejected.
          */
            //discard everything in the port.
            byte[] discarded = readAvailable();
            write(new byte[]
            {
                RQ_CONNECTION_REQUEST, MAJOR_VERSION, MINOR_VERSION
            });

            byte handshake = readNext();
            byte send = (byte)((MAJOR_VERSION + MINOR_VERSION + handshake) % 256);
            write(new byte[] { send });

            byte ack = readNext();

            if (send != ack)
            {
                if (retries > 0)
                {
                    System.Threading.Thread.Sleep(50);
                    performHandshake(retries - 1);
                }
                else
                {
                    throw new InvalidOperationException("Could not perform handshake with the Arduino");
                }
            }


        }
        public void runProgram(byte[] program)
        {
            port.Open();
            performHandshake();
            write(program);
            port.Close();

        }
        ~UziConnection()
        {
            port?.Close();
            port = null;
        }
    }
}
