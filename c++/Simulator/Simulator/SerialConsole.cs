using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
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
            sketch.WriteSerial(inputTextBox.Text);
            inputTextBox.Clear();
        }

        private void updateTimer_Tick(object sender, EventArgs e)
        {
            Tuple<DateTime, string> output = sketch.ReadSerial();

            if (output == null) return;
            outputTextBox.AppendText(string.Format("{0:HH:mm:ss.fff}| {1}", output.Item1, output.Item2));
            outputTextBox.AppendText(Environment.NewLine);
        }
    }
}
