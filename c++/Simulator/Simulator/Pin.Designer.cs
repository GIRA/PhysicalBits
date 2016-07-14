namespace Simulator
{
    partial class Pin
    {
        /// <summary> 
        /// Variable del diseñador requerida.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary> 
        /// Limpiar los recursos que se estén utilizando.
        /// </summary>
        /// <param name="disposing">true si los recursos administrados se deben eliminar; false en caso contrario, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Código generado por el Diseñador de componentes

        /// <summary> 
        /// Método necesario para admitir el Diseñador. No se puede modificar 
        /// el contenido del método con el editor de código.
        /// </summary>
        private void InitializeComponent()
        {
            this.title = new System.Windows.Forms.Label();
            this.numericUpDown1 = new System.Windows.Forms.NumericUpDown();
            this.led = new System.Windows.Forms.PictureBox();
            this.graph = new Simulator.Graph();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDown1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.led)).BeginInit();
            this.SuspendLayout();
            // 
            // title
            // 
            this.title.BackColor = System.Drawing.Color.White;
            this.title.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.title.Font = new System.Drawing.Font("Calibri", 24F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.title.ForeColor = System.Drawing.Color.Black;
            this.title.Location = new System.Drawing.Point(10, 0);
            this.title.Name = "title";
            this.title.Size = new System.Drawing.Size(92, 37);
            this.title.TabIndex = 0;
            this.title.Text = "00";
            this.title.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // numericUpDown1
            // 
            this.numericUpDown1.Increment = new decimal(new int[] {
            50,
            0,
            0,
            0});
            this.numericUpDown1.Location = new System.Drawing.Point(10, 40);
            this.numericUpDown1.Maximum = new decimal(new int[] {
            1023,
            0,
            0,
            0});
            this.numericUpDown1.Name = "numericUpDown1";
            this.numericUpDown1.Size = new System.Drawing.Size(92, 20);
            this.numericUpDown1.TabIndex = 1;
            this.numericUpDown1.ValueChanged += new System.EventHandler(this.numericUpDown1_ValueChanged);
            // 
            // led
            // 
            this.led.BackColor = System.Drawing.Color.White;
            this.led.Image = global::Simulator.Properties.Resources.off;
            this.led.Location = new System.Drawing.Point(21, 66);
            this.led.Name = "led";
            this.led.Size = new System.Drawing.Size(69, 66);
            this.led.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.led.TabIndex = 3;
            this.led.TabStop = false;
            this.led.Click += new System.EventHandler(this.led_Click);
            // 
            // graph
            // 
            this.graph.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.graph.Location = new System.Drawing.Point(110, -2);
            this.graph.Name = "graph";
            this.graph.Size = new System.Drawing.Size(37, 166);
            this.graph.TabIndex = 2;
            // 
            // Pin
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.White;
            this.Controls.Add(this.led);
            this.Controls.Add(this.numericUpDown1);
            this.Controls.Add(this.title);
            this.Controls.Add(this.graph);
            this.ForeColor = System.Drawing.Color.Black;
            this.Margin = new System.Windows.Forms.Padding(0);
            this.Name = "Pin";
            this.Size = new System.Drawing.Size(147, 164);
            this.Load += new System.EventHandler(this.Pin_Load);
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDown1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.led)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Label title;
        private System.Windows.Forms.NumericUpDown numericUpDown1;
        private Graph graph;
        private System.Windows.Forms.PictureBox led;
    }
}
