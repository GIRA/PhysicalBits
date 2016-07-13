using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Simulator
{
    public partial class SerialConsole : Form
    {
        private Sketch sketch;

        public SerialConsole(Sketch sketch)
        {
            this.sketch = sketch;

            InitializeComponent();
        }

        private void SerialConsole_Load(object sender, EventArgs e)
        {
            updateTimer.Enabled = true;
        }

        private void sendButton_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(inputTextBox.Text)) return;
            sketch.WriteSerial(Parse(inputTextBox.Text));
            inputTextBox.Clear();
        }

        private void updateTimer_Tick(object sender, EventArgs e)
        {
            Tuple<DateTime, byte[]> output = sketch.ReadSerial();

            if (output == null) return;

            string ascii = Encoding.ASCII.GetString(output.Item2);
            asciiTextBox.AppendText(string.Format("{0:HH:mm:ss.fff} | {1}", output.Item1, ascii));
            asciiTextBox.AppendText(Environment.NewLine);


            string hex = string.Join(" ", output.Item2.Select(b => b.ToString("X2")));
            hexTextBox.AppendText(string.Format("{0:HH:mm:ss.fff} | {1}", output.Item1, hex));
            hexTextBox.AppendText(Environment.NewLine);
            
            string dec = string.Join(" ", output.Item2);
            decTextBox.AppendText(string.Format("{0:HH:mm:ss.fff} | {1}", output.Item1, dec));
            decTextBox.AppendText(Environment.NewLine);
        }

        private byte[] Parse(string input)
        {
            try {
                string trimmed = input.Trim();
                if (Regex.IsMatch(trimmed, "^'(.|\n)*'\\z"))
                {
                    // string
                    return Encoding.ASCII
                        .GetBytes(trimmed
                            .Substring(1, trimmed.Length - 2)
                            .Replace("''", "'"));
                }
                else if (Regex.IsMatch(trimmed, @"^#\[(\d|\s)*\]\z"))
                {
                    // array
                    return trimmed
                        .Substring(2, trimmed.Length - 3)
                        .Split(' ', '\n', '\r', '\t')
                        .Where(str => !string.IsNullOrWhiteSpace(str))
                        .Select(str => byte.Parse(str))
                        .ToArray();
                }
            }
            catch { /* Do nothing */ }

            // I give up, just treat as text
            return Encoding.ASCII.GetBytes(input);
        }
    }
}
