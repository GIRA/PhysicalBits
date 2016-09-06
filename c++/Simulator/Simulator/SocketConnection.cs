using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Simulator
{
    class SocketConnection
    {
        private Sketch sketch;
        private int port;

        private TcpListener listener;
        private Thread thread;
        private ConcurrentQueue<Tuple<DateTime, byte[]>> serial;
        private bool running = false;
        private bool connected = false;

        public SocketConnection(Sketch sketch, int port = 4242)
        {
            this.sketch = sketch;
            this.port = port;

            sketch.SerialReceived += OnSerialReceived;
            listener = new TcpListener(IPAddress.Any, port);
            listener.Start();
        }

        public int Port { get { return port; } }
        public bool Running { get { return running; } }
        public bool Connected { get { return connected; } }

        private void OnSerialReceived(Tuple<DateTime, byte[]> obj)
        {
            if (connected)
            {
                serial.Enqueue(obj);
            }
        }

        public void Start()
        {
            if (running) return;

            serial = new ConcurrentQueue<Tuple<DateTime, byte[]>>();
            thread = new Thread(Step);
            thread.IsBackground = true;
            thread.Start();
        }

        public void Stop()
        {
            running = false;
        }

        private void Step()
        {
            running = true;
            do
            {
                using (Socket client = listener.AcceptSocket())
                {
                    connected = true;
                    do
                    {
                        try
                        {
                            // Serial to Socket
                            Tuple<DateTime, byte[]> tuple;
                            while (serial.TryDequeue(out tuple))
                            {
                                client.Send(tuple.Item2);
                            }

                            // Socket to serial
                            byte[] buffer = new byte[1024];
                            int bytesRead = client.Receive(buffer);
                            sketch.WriteSerial(buffer.Take(bytesRead).ToArray());

                            // Break if we're no longer running
                            connected = running;
                        }
                        catch
                        {
                            connected = false;
                        }
                    }
                    while (connected);
                }
            }
            while (running);
        }
    }
}
