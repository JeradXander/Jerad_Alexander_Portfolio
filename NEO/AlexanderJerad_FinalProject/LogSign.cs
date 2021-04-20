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
using MySql.Data.MySqlClient;

namespace AlexanderJerad_FinalProject
{
    public partial class LogSign : Form
    {
        //event handler for getting user
        public EventHandler getUser;
        bool Xbutton = true;
        //mysql conne
        MySqlConnection conn;

        private User signedUser;
        //user variable
        internal User SignedUser { get => signedUser; set => signedUser = value; }

        public LogSign()
        {
            InitializeComponent();
            HandleClientWindowSize();
        }

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

        private void LogSign_FormClosed(object sender, FormClosedEventArgs e)
        {
            //if xbutton is used conditional application will close
            if (Xbutton == true)
            {
                Application.Exit();
            }
            else
            {
            }

        }
        
        //signin event
        private void button1_Click(object sender, EventArgs e)
        {
            //trying to complete signing
            try
            {

                string connString = buildConnectionString();

                conn = new MySqlConnection(connString);
                conn.Open();
                //values to passthrough to database
                string usn = textUserName.Text.ToLower();
                string pwd = textPassword.Text;

                User myUser = new User();
                //going to login method in users
                myUser.UserLogin(conn, usn, pwd);
                //conditional if user is valid
                if (myUser.Userid != null)
                {
                    signedUser = myUser;
                    //event 
                    getUser(this, new EventArgs());

                    MessageBox.Show("Signed In complete");
                    Xbutton = false;
                    this.Close();
                }
                else
                {
                    //user was not valid or in the database
                    MessageBox.Show("username/password not found please try again.");
                }
            }
            catch (MySqlException er)
            {
                MessageBox.Show(er.ToString());
            }
            finally
            {
                if (conn != null)
                { //if we did connect and open

                    conn.Close();
                }
            }
        }
       
        //signup event 
        private void buttonSUComp_Click(object sender, EventArgs e)
        {

            try
            {
                //new user variable
                User newUser = new User();
                //getting string to connect
                string connString = buildConnectionString();
                //connecting
                conn = new MySqlConnection(connString);

                //conditional for checking if the passwords are the same 
                if (textPassword.Text == textConfirm.Text)
                {
                    //creating new user
                    DateTime newdate;
                    newUser.Firstn = textFirst.Text.ToLower();
                    newUser.Lastn = textLast.Text.ToLower();
                    newUser.Password = textPassword.Text;
                    newUser.Email = textBox1.Text.ToLower();
                    newUser.Username = textUserName.Text.ToLower();

                    DateTime.TryParse(dateTimePicker1.Text, out newdate);
                    newUser.Dob = newdate;

                    //saving the user to database
                    newUser.SaveUSer(conn, newUser.Username, newUser.Password);
                    MessageBox.Show($"Signup Complete\n\nUsername:  {newUser.Username}\nPassword:  {newUser.Password}\n\nSigning In ");
                    //logging in the user to database
                    newUser.UserLogin(conn, newUser.Username, newUser.Password);

                    //geeting the user to send to main form
                    if (newUser.Userid != null)
                    {
                        signedUser = newUser;
                        getUser(this, new EventArgs());
                        Xbutton = false;
                        this.Close();
                    }
                }
                else
                {
                    MessageBox.Show("the passwords did not match!\nPlease try again.");
                }
            }
            catch (MySqlException error)
            {
                MessageBox.Show(error.ToString());
            }
            finally
            {
                if (conn != null)
                { //if we did connect and open

                    conn.Close();
                }
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

        private void textPassword_Click(object sender, EventArgs e)
        {
            textPassword.Clear();
        }

        private void textUserName_Click(object sender, EventArgs e)
        {
            textUserName.Clear();
        }

        private void textFirst_Click(object sender, EventArgs e)
        {
            textFirst.Clear();
        }

        private void textLast_Click(object sender, EventArgs e)
        {
            textLast.Clear();
        }

        private void buttonSI_Click(object sender, EventArgs e)
        {
            buttonSI.Visible = false;
            buttonSU.Visible = false;
            buttonBack.Visible = true;
            label1.Visible = true;
            label2.Visible = true;
            buttonSIComp.Visible = true;
            textUserName.Visible = true;
            textPassword.Visible = true;
        }

        private void textConfirm_Click(object sender, EventArgs e)
        {
            textConfirm.Clear();
        }

        private void textBox1_Click(object sender, EventArgs e)
        {
            textBox1.Clear();
        }

        private void buttonSU_Click(object sender, EventArgs e)
        {
            buttonSI.Visible = false;
            buttonSU.Visible = false;
            buttonBack.Visible = true;
            label1.Visible = true;
            label2.Visible = true;
            label3.Visible = true;
            label4.Visible = true;
            label5.Visible = true;
            label6.Visible = true;
            label7.Visible = true;
            buttonSUComp.Visible = true;
            textUserName.Visible = true;
            textPassword.Visible = true;
            textConfirm.Visible = true;
            textFirst.Visible = true;
            textLast.Visible = true;
            textBox1.Visible = true;
            dateTimePicker1.Visible = true;
        }

        private void buttonBack_Click(object sender, EventArgs e)
        {
            buttonSI.Visible = true;
            buttonSU.Visible = true;
            buttonBack.Visible = false;
            buttonSIComp.Visible = false;
            label1.Visible = false;
            label2.Visible = false;
            label3.Visible = false;
            label4.Visible = false;
            label5.Visible = false;
            label6.Visible = false;
            label7.Visible = false;
            buttonSUComp.Visible = false;
            textUserName.Visible = false;
            textPassword.Visible = false;
            textConfirm.Visible = false;
            textFirst.Visible = false;
            textLast.Visible = false;
            textBox1.Visible = false;
            dateTimePicker1.Visible = false;
        }
    }
}
