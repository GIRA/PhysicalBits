using System;
using System.Linq;

namespace Simulator
{
    public class ExecutionSnapshot
    {
        public int ms;
        public byte[] pins = new byte[4];
        public byte error;

        public bool IsDifferentThan(ExecutionSnapshot other)
        {
            return other == null || !pins.SequenceEqual(other.pins);
        }

        public override string ToString()
        {
            return ms + "," + string.Join(",", pins);
        }
    }
}