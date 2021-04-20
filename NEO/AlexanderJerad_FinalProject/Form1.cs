using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO;
using System.Net;
using Newtonsoft.Json.Linq;
using MySql.Data.MySqlClient;

namespace AlexanderJerad_FinalProject
{
    public partial class Main : Form
    {
        //connection string variable
        MySqlConnection conn = new MySqlConnection();

        //all variables
        private User signedUser;
        string current = Directory.GetCurrentDirectory();
        public Image landBackground;
        public Image portBackground;
        private string landDir;
        private string portDir;
        private List<NEO> neoList = new List<NEO>();
        //connecting to api
        WebClient apiConnection = new WebClient();
        private string todaysDate = DateTime.Today.ToString("yyyy-MM-dd");
        private string chosenDate;
        //partss of api
        private string endOfapi = "&api_key=MpFivttwqenSbo9mogULih793pP8xaYRASfpX2Zp";
        private string startingAPI = "https://api.nasa.gov/neo/rest/v1/feed?start_date=";
        private string apiLink = "&end_date=";
        private string finalAPI;
        private string newApi;




        public Main()
        {
            //getting user
            LogSign log = new LogSign();
            //user event
            log.getUser += HandleGetUser;

            //showing signin form first
            log.ShowDialog();

            //setting form 1
            InitializeComponent();

            // window method
            HandleClientWindowSize();
            //getting todays neos
            GetTodaysNeos();
            //if user was signed in 
            if (signedUser != null)
            {
                labelWelcome.Text = $"WELCOME {signedUser.Firstn.ToUpper()}!!";
                dateTimePicker1.Text = signedUser.Dob.ToShortDateString();
            }

            //tooltips for buttons
            toolTipDate.SetToolTip(dateTimePicker1,"Change date to populate the list of NEO'S for that date.");
            toolTipDate.InitialDelay = 350;
            toolTipOpen.SetToolTip(buttonOpen, "This will open The official Nasa page in your browser.");
            toolTipOpen.InitialDelay = 350;
            toolTipSave.SetToolTip(buttonSave,"This will save the current list to our database.");
            toolTipSave.InitialDelay = 350;

        }

        private void GetTodaysNeos()
        {
            //api string
            finalAPI = startingAPI + todaysDate + apiLink + todaysDate + endOfapi;
            //json reading method
            ReadJsonData(finalAPI, todaysDate);
        }

        private void ReadJsonData(string api, string date)
        {
            //try to check internet connection
            try
            {
                string numberOfNeos;
                
                //connecting to internet
                var apiData = apiConnection.DownloadString(api);

                //creating json object
                JObject o = JObject.Parse(apiData);
                int numOfNeos;
                //getting number of neos for that day for loop
                numberOfNeos = o["element_count"].ToString();
                int.TryParse(numberOfNeos, out numOfNeos);

                //loop through data

                for (int i = 0; i < numOfNeos; i++)
                {
                    //values
                    string neoName;
                    string diameter;
                    decimal diam;
                    string url;
                    string threat;
                    bool thrt;
                    string speed;
                    decimal mph;
                    
                    NEO newNeo = new NEO();
                    //json data
                    neoName = o["near_earth_objects"][date][i]["name"].ToString();
                    diameter = o["near_earth_objects"][date][i]["estimated_diameter"]["meters"]["estimated_diameter_max"].ToString();
                    url = o["near_earth_objects"][date][i]["nasa_jpl_url"].ToString();
                    threat = o["near_earth_objects"][date][i]["is_potentially_hazardous_asteroid"].ToString();
                    speed = o["near_earth_objects"][date][i]["close_approach_data"][0]["relative_velocity"]["miles_per_hour"].ToString();

                    newNeo.Name = neoName;
                    decimal.TryParse(diameter, out diam);
                    
                    newNeo.Diameter = decimal.Round(diam, 2);
                    newNeo.Url = url;
                    bool.TryParse(threat, out thrt);
                    newNeo.Threat = thrt;
                    decimal.TryParse(speed, out mph);
                   
                    newNeo.Mph = decimal.Round(mph, 2); 
                    //adding to list
                    neoList.Add(newNeo);

                    apiData = null;
                }
                //loop to add neos to list
                foreach (NEO neo in neoList)
                {
                    listBoxNEO.Items.Add(neo);
                }

            }//exception
            catch (Exception e)
            {
                MessageBox.Show(e.ToString());
            }


        }

        //setting user thats signed in event
        public void HandleGetUser(object sender, EventArgs e)
        {
            LogSign log = sender as LogSign;

            signedUser = log.SignedUser;

        }

        //setting clients window size
        void HandleClientWindowSize()
        {
            //Modify ONLY these float values
            float HeightValueToChange = 1.4f;
            float WidthValueToChange = 6.0f;

            //DO NOT MODIFY THIS CODE
            int height = Convert.ToInt32(Screen.PrimaryScreen.WorkingArea.Size.Height / HeightValueToChange);
            int width = Convert.ToInt32(Screen.PrimaryScreen.WorkingArea.Size.Width / WidthValueToChange);
            if (height < Size.Height)
                height = Size.Height;
            if (width < Size.Width)
                width = Size.Width;
            this.Size = new Size(width, height);
            //this.Size = new Size(376, 720);

        }

        //Rotating method 
        void HandleRotate()
        {
            //if landscape is checked
            if (landscapeToolStripMenuItem.Checked == true && landBackground == null)
            {
                //changing layout. the same for the rest of the conditional
                this.groupBox1.Location = new Point(85, 20);
                this.groupBox1.Size = new Size(502, 261);


                landDir = current + @"\iPhone7Imageland.jpg";
                landBackground = new Bitmap(landDir);
                landBackground.RotateFlip(RotateFlipType.Rotate90FlipXY);

                this.Size = new Size(1351 / 2, 687 / 2);
                this.BackgroundImage = landBackground;
                this.BackgroundImageLayout = ImageLayout.Stretch;
                
            }
            else if (landscapeToolStripMenuItem.Checked == true && landBackground != null)
            {
                this.groupBox1.Location = new Point(85, 20);
                this.groupBox1.Size = new Size(502, 261);

                this.Size = new Size(1351 / 2, 687 / 2);
                this.BackgroundImage = landBackground;
                this.BackgroundImageLayout = ImageLayout.Stretch;
            }
            //if portrait is checked
            else if (portraitToolStripMenuItem.Checked == true && portBackground == null)
            {
                this.groupBox1.Location = new Point(25, 90);
                this.groupBox1.Size = new Size(280, 477);


                portDir = current + @"\iPhone7Imageport.jpg";
                portBackground = new Bitmap(portDir);

                this.Size = new Size(687 / 2, 1351 / 2);
                this.BackgroundImage = portBackground;
                this.BackgroundImageLayout = ImageLayout.Stretch;
            }
            else if (portraitToolStripMenuItem.Checked == true && portBackground != null)
            {
                this.groupBox1.Location = new Point(25, 90);
                this.groupBox1.Size = new Size(280, 477);


                portBackground = new Bitmap(portDir);

                this.Size = new Size(687 / 2, 1351 / 2);
                this.BackgroundImage = portBackground;
                this.BackgroundImageLayout = ImageLayout.Stretch;
            }


        }

        private void landscapeToolStripMenuItem_Click(object sender, EventArgs e)
        {
            
            landscapeToolStripMenuItem.Checked = true;
            portraitToolStripMenuItem.Checked = false;
            //moving layout 
            groupBox2.Location = new Point(250, 58);

            //rotating method
            HandleRotate();
        }

        //changing to portrait orientation
        private void portraitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            //moving layout around
            portraitToolStripMenuItem.Checked = true;
            landscapeToolStripMenuItem.Checked = false;
            //moving groupbox
            groupBox2.Location = new Point(6, 250);
            //rotating method
            HandleRotate();
        }

        //exit function
        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }

        //opeing nasa browser
        private void button1_Click(object sender, EventArgs e)
        {
            //opening browser 
            if (listBoxNEO.SelectedIndex >= 0)
            {
                //opening the selected neos url
                int selected = listBoxNEO.SelectedIndex;
                System.Diagnostics.Process.Start($"{neoList[selected].Url}");
            }
            else
            {
                MessageBox.Show("please select NEO to view.");
            }
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (listBoxNEO.SelectedIndex >= 0)
            {
                //getting the values
                textBoxName.Text = neoList[listBoxNEO.SelectedIndex].Name;
                numSize.Value = neoList[listBoxNEO.SelectedIndex].Diameter;
                checkThreat.Checked = neoList[listBoxNEO.SelectedIndex].Threat;
                numSpeed.Value = neoList[listBoxNEO.SelectedIndex].Mph;

            }
            else
            {
                MessageBox.Show("Please select a new to view");
            }

        }

        //event for date change
        private void dateTimePicker1_ValueChanged(object sender, EventArgs e)
        {
            //clearing list
            neoList.Clear();
            listBoxNEO.Items.Clear();

            //getting new date
            chosenDate = dateTimePicker1.Value.ToString("yyyy-MM-dd");

           //new api string
            newApi = startingAPI + chosenDate + apiLink + chosenDate + endOfapi;

           //getting data for the date
            ReadJsonData(newApi, chosenDate);

        }

        //saving neo list to database
        private void buttonSave_Click(object sender, EventArgs e)
        {
            try
            {
                //building connection string
                string connString = buildConnectionString();

                //connecting to mysqp
                conn = new MySqlConnection(connString);

                //replacing neos if they dont already exist
                for (int i = 0; i < neoList.Count;i++ ) 
                {
                    NEO neo = neoList[i];
                    conn.Open();
                    //sql statement
                    //database only updates if neo does not exist.
                    string upSql = $"INSERT ignore neos (name, speed, size, threat, url) " +
                        $"SELECT '{neo.Name}',{neo.Mph},{neo.Diameter},{neo.Threat},'{neo.Url}' " +
                        $"FROM neos " +
                        $" WHERE NOT EXISTS(SELECT *" +
                        $" FROM neos WHERE name = '{neo.Name}'" +
                        $" AND speed = {neo.Mph}" +
                        $" AND size = {neo.Diameter}" +
                        $" And threat = {neo.Threat}" +
                        $" And url = '{neo.Url}') limit 1;";

                   

                    //Create the DataAdapter
                    MySqlCommand upcmd = new MySqlCommand(upSql, conn);
                    MySqlDataReader reader1;

                    reader1 = upcmd.ExecuteReader();
                    //closing
                    conn.Close();
                }
                MessageBox.Show("neos saved");
               
            }//exception
            catch (MySqlException ex)
            {
                MessageBox.Show($"UH_OH\n\n{ex.ToString()}");
            }



        }

        //building connection string method
        private string buildConnectionString()
        {
            string serverIp = "";
            try
            {
                //open text file using streamreader 
                using (StreamReader reader = new StreamReader("C:\\VFW\\Connect.txt"))
                {
                    serverIp = reader.ReadToEnd();
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.ToString());
            }
            return $"server={serverIp};userid=dbremoteuser;password=password;database=Final Project;port=8889;SslMode =none";
        }

    }
}
