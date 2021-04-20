namespace AlexanderJerad_FinalProject
{
    partial class Main
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Main));
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.orientationToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.landscapeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.portraitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.buttonSave = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.label4 = new System.Windows.Forms.Label();
            this.numSpeed = new System.Windows.Forms.NumericUpDown();
            this.checkThreat = new System.Windows.Forms.CheckBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.numSize = new System.Windows.Forms.NumericUpDown();
            this.textBoxName = new System.Windows.Forms.TextBox();
            this.buttonOpen = new System.Windows.Forms.Button();
            this.labelWelcome = new System.Windows.Forms.Label();
            this.listBoxNEO = new System.Windows.Forms.ListBox();
            this.dateTimePicker1 = new System.Windows.Forms.DateTimePicker();
            this.toolTipDate = new System.Windows.Forms.ToolTip(this.components);
            this.toolTipSave = new System.Windows.Forms.ToolTip(this.components);
            this.toolTipOpen = new System.Windows.Forms.ToolTip(this.components);
            this.menuStrip1.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numSpeed)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.numSize)).BeginInit();
            this.SuspendLayout();
            // 
            // menuStrip1
            // 
            this.menuStrip1.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.orientationToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(3, 27);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(566, 42);
            this.menuStrip1.TabIndex = 1;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(64, 38);
            this.fileToolStripMenuItem.Text = "File";
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(151, 38);
            this.exitToolStripMenuItem.Text = "Exit";
            this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
            // 
            // orientationToolStripMenuItem
            // 
            this.orientationToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.landscapeToolStripMenuItem,
            this.portraitToolStripMenuItem});
            this.orientationToolStripMenuItem.Name = "orientationToolStripMenuItem";
            this.orientationToolStripMenuItem.Size = new System.Drawing.Size(144, 38);
            this.orientationToolStripMenuItem.Text = "orientation";
            // 
            // landscapeToolStripMenuItem
            // 
            this.landscapeToolStripMenuItem.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.landscapeToolStripMenuItem.Name = "landscapeToolStripMenuItem";
            this.landscapeToolStripMenuItem.Size = new System.Drawing.Size(324, 38);
            this.landscapeToolStripMenuItem.Text = "Landscape";
            this.landscapeToolStripMenuItem.Click += new System.EventHandler(this.landscapeToolStripMenuItem_Click);
            // 
            // portraitToolStripMenuItem
            // 
            this.portraitToolStripMenuItem.Checked = true;
            this.portraitToolStripMenuItem.CheckState = System.Windows.Forms.CheckState.Checked;
            this.portraitToolStripMenuItem.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.portraitToolStripMenuItem.Name = "portraitToolStripMenuItem";
            this.portraitToolStripMenuItem.Size = new System.Drawing.Size(324, 38);
            this.portraitToolStripMenuItem.Text = "Portrait";
            this.portraitToolStripMenuItem.Click += new System.EventHandler(this.portraitToolStripMenuItem_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.buttonSave);
            this.groupBox1.Controls.Add(this.label1);
            this.groupBox1.Controls.Add(this.groupBox2);
            this.groupBox1.Controls.Add(this.labelWelcome);
            this.groupBox1.Controls.Add(this.listBoxNEO);
            this.groupBox1.Controls.Add(this.dateTimePicker1);
            this.groupBox1.Controls.Add(this.menuStrip1);
            this.groupBox1.Location = new System.Drawing.Point(47, 166);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(572, 904);
            this.groupBox1.TabIndex = 2;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "N.E.O Dates";
            // 
            // buttonSave
            // 
            this.buttonSave.Location = new System.Drawing.Point(259, 137);
            this.buttonSave.Name = "buttonSave";
            this.buttonSave.Size = new System.Drawing.Size(160, 95);
            this.buttonSave.TabIndex = 13;
            this.buttonSave.Text = "Save list to database";
            this.buttonSave.UseVisualStyleBackColor = true;
            this.buttonSave.Click += new System.EventHandler(this.buttonSave_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(6, 198);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(110, 37);
            this.label1.TabIndex = 8;
            this.label1.Text = "N.E.O";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.label4);
            this.groupBox2.Controls.Add(this.numSpeed);
            this.groupBox2.Controls.Add(this.checkThreat);
            this.groupBox2.Controls.Add(this.label3);
            this.groupBox2.Controls.Add(this.label2);
            this.groupBox2.Controls.Add(this.numSize);
            this.groupBox2.Controls.Add(this.textBoxName);
            this.groupBox2.Controls.Add(this.buttonOpen);
            this.groupBox2.Location = new System.Drawing.Point(13, 498);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(466, 348);
            this.groupBox2.TabIndex = 7;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Neo Info";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(20, 191);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(135, 25);
            this.label4.TabIndex = 12;
            this.label4.Text = "Speed (mph)";
            // 
            // numSpeed
            // 
            this.numSpeed.DecimalPlaces = 2;
            this.numSpeed.Location = new System.Drawing.Point(172, 189);
            this.numSpeed.Maximum = new decimal(new int[] {
            100000,
            0,
            0,
            0});
            this.numSpeed.Name = "numSpeed";
            this.numSpeed.ReadOnly = true;
            this.numSpeed.Size = new System.Drawing.Size(248, 31);
            this.numSpeed.TabIndex = 11;
            // 
            // checkThreat
            // 
            this.checkThreat.AutoSize = true;
            this.checkThreat.Location = new System.Drawing.Point(172, 145);
            this.checkThreat.Name = "checkThreat";
            this.checkThreat.Size = new System.Drawing.Size(197, 29);
            this.checkThreat.TabIndex = 10;
            this.checkThreat.Text = "Potential Impact";
            this.checkThreat.UseVisualStyleBackColor = true;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(16, 97);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(139, 25);
            this.label3.TabIndex = 9;
            this.label3.Text = "Size (meters)";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(87, 48);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(68, 25);
            this.label2.TabIndex = 8;
            this.label2.Text = "Name";
            // 
            // numSize
            // 
            this.numSize.DecimalPlaces = 2;
            this.numSize.Location = new System.Drawing.Point(172, 95);
            this.numSize.Maximum = new decimal(new int[] {
            100000,
            0,
            0,
            0});
            this.numSize.Name = "numSize";
            this.numSize.ReadOnly = true;
            this.numSize.Size = new System.Drawing.Size(248, 31);
            this.numSize.TabIndex = 7;
            // 
            // textBoxName
            // 
            this.textBoxName.Location = new System.Drawing.Point(172, 45);
            this.textBoxName.Name = "textBoxName";
            this.textBoxName.Size = new System.Drawing.Size(248, 31);
            this.textBoxName.TabIndex = 6;
            // 
            // buttonOpen
            // 
            this.buttonOpen.Location = new System.Drawing.Point(172, 241);
            this.buttonOpen.Name = "buttonOpen";
            this.buttonOpen.Size = new System.Drawing.Size(248, 73);
            this.buttonOpen.TabIndex = 5;
            this.buttonOpen.Text = "Open NASA Page";
            this.buttonOpen.UseVisualStyleBackColor = true;
            this.buttonOpen.Click += new System.EventHandler(this.button1_Click);
            // 
            // labelWelcome
            // 
            this.labelWelcome.AutoSize = true;
            this.labelWelcome.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelWelcome.Location = new System.Drawing.Point(6, 87);
            this.labelWelcome.Name = "labelWelcome";
            this.labelWelcome.Size = new System.Drawing.Size(128, 37);
            this.labelWelcome.TabIndex = 6;
            this.labelWelcome.Text = "dsfsadf";
            // 
            // listBoxNEO
            // 
            this.listBoxNEO.FormattingEnabled = true;
            this.listBoxNEO.ItemHeight = 25;
            this.listBoxNEO.Location = new System.Drawing.Point(13, 238);
            this.listBoxNEO.Name = "listBoxNEO";
            this.listBoxNEO.Size = new System.Drawing.Size(406, 254);
            this.listBoxNEO.TabIndex = 4;
            this.listBoxNEO.SelectedIndexChanged += new System.EventHandler(this.listBox1_SelectedIndexChanged);
            // 
            // dateTimePicker1
            // 
            this.dateTimePicker1.Format = System.Windows.Forms.DateTimePickerFormat.Short;
            this.dateTimePicker1.Location = new System.Drawing.Point(13, 137);
            this.dateTimePicker1.Name = "dateTimePicker1";
            this.dateTimePicker1.Size = new System.Drawing.Size(206, 31);
            this.dateTimePicker1.TabIndex = 3;
            this.dateTimePicker1.ValueChanged += new System.EventHandler(this.dateTimePicker1_ValueChanged);
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(12F, 25F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackgroundImage = ((System.Drawing.Image)(resources.GetObject("$this.BackgroundImage")));
            this.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Stretch;
            this.ClientSize = new System.Drawing.Size(661, 1280);
            this.Controls.Add(this.groupBox1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Main";
            this.Text = "Main";
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.numSpeed)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.numSize)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem orientationToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem landscapeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem portraitToolStripMenuItem;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.DateTimePicker dateTimePicker1;
        private System.Windows.Forms.ListBox listBoxNEO;
        private System.Windows.Forms.Button buttonOpen;
        private System.Windows.Forms.Label labelWelcome;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.NumericUpDown numSize;
        private System.Windows.Forms.TextBox textBoxName;
        private System.Windows.Forms.CheckBox checkThreat;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.NumericUpDown numSpeed;
        private System.Windows.Forms.Button buttonSave;
        private System.Windows.Forms.ToolTip toolTipDate;
        private System.Windows.Forms.ToolTip toolTipSave;
        private System.Windows.Forms.ToolTip toolTipOpen;
    }
}

