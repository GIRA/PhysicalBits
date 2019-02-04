using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Simulator
{
    public class SketchRecorder
    {
        private byte[] program;
        private byte[] pinMap = { 9, 10, 11, 12 };
        private int targetTime;

        private readonly byte[] emptyProgram = { 0, 0, 0 };
        private Sketch sketch;
        //TODO(Tera): this definitions should be centralized, probably on the sketch
        private const byte RQ_CONNECTION_REQUEST = 255;
        private const byte MAJOR_VERSION = 0;
        private const byte MINOR_VERSION = 6;
        private const byte KEEP_ALIVE = 7;


        private int currentTime = 0;
        private ExecutionSnapshot lastSnapshot = null;

        public SketchRecorder(byte[] program, int time)
        {
            this.program = program;
            this.targetTime = time;
            initializeSketch();
        }

        private void initializeSketch()
        {
            sketch = Sketch.Current;
            sketch.RegisterStats(false);

            sketch.SetMillis(0);
            sketch.Setup();

            performHandshake();
            // load an empty program, just in case.
            sketch.WriteSerial(emptyProgram);

            sketch.Loop();


        }

        private void performHandshake()
        {
            /*
          * INFO(Richo): Perform connection request and handshake.
          * Otherwise, when we send a program later we will be rejected.
          */
            sketch.WriteSerial(new byte[]
            {
                RQ_CONNECTION_REQUEST, MAJOR_VERSION, MINOR_VERSION
            });
            sketch.Loop();
            byte handshake = sketch.ReadSerial().Item2[0];
            byte send = (byte)((MAJOR_VERSION + MINOR_VERSION + handshake) % 256);
            sketch.WriteSerial(new byte[] { send });
            sketch.Loop();
            byte ack = sketch.ReadSerial().Item2[0];

            if (send != ack) { throw new InvalidOperationException("Could not perform handshake with the simulator"); }


        }

        public static IEnumerable<ExecutionSnapshot> RecordExecution(byte[] program, int time)
        {
            return new SketchRecorder(program, time).getInterestingSnapshots();

        }


        private IEnumerable<ExecutionSnapshot> getSnapshots()
        {
            sketch.WriteSerial(program);
            while (currentTime <= targetTime)
            {

                sketch.SetMillis(currentTime);

                sketch.Loop();
                ExecutionSnapshot currentSnapshot = new ExecutionSnapshot();
                currentSnapshot.ms = currentTime;
                for (int i = 0; i < currentSnapshot.pins.Length; i++)
                {
                    currentSnapshot.pins[i] = (byte)(sketch.GetPinValue(pinMap[i]) == 0 ? 0 : 1);
                }
                yield return currentSnapshot;
                currentTime++;
            }
        }
        private IEnumerable<ExecutionSnapshot> getInterestingSnapshots()
        {
            lastSnapshot = null;
            foreach (var currentSnapshot in getSnapshots())
            {
                if (currentSnapshot.IsDifferentThan(lastSnapshot))
                {
                    lastSnapshot = currentSnapshot;
                    yield return lastSnapshot;
                }
            }
        }
    }
}
