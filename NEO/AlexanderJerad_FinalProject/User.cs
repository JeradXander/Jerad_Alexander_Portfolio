using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MySql.Data.MySqlClient;

namespace AlexanderJerad_FinalProject
{
    class User
    {
        //values
        private string userid;
        private string firstn;
        private string lastn;
        private string username;
        private string password;
        private string email;
        private DateTime dob;

        //properties
        public string Firstn { get => firstn; set => firstn = value; }
        public string Lastn { get => lastn; set => lastn = value; }
        public string Username { get => username; set => username = value; }
        public string Password { get => password; set => password = value; }
        public string Email { get => email; set => email = value; }
        public string Userid { get => userid; set => userid = value; }
        public DateTime Dob { get => dob; set => dob = value; }


        // Method for user login
        public void UserLogin(MySqlConnection conn, string userName, string password)
        {
            //msql query
            string stm = "SELECT userId, firstname, lastname, dob FROM users WHERE  username = @username AND md5 = MD5(@password)";

            MySqlCommand cmd = new MySqlCommand(stm, conn);

            cmd.Parameters.AddWithValue("@username", userName);
            cmd.Parameters.AddWithValue("@password", password);
            MySqlDataReader rdr = cmd.ExecuteReader();

            if (rdr.HasRows)
            {
                rdr.Read();
                userid = rdr["userId"].ToString();
                firstn = rdr["firstName"].ToString();
                lastn= rdr["lastname"].ToString();
               
              
                DateTime.TryParse(rdr["dob"].ToString(), out dob);
                //End of user login
            }
            rdr.Close();
        }

        //method for creating user 
        public void SaveUSer(MySqlConnection conn, string username, string password)
        {
            conn.Open();
            //statemanent to insert info
            string stm = "Insert INTO users ( firstname, lastname, email, username, password, dob, md5) Values (@fn,@ln,@em,@un,@pwd,@dob, MD5(@pwd));";

            MySqlCommand cmd = new MySqlCommand(stm, conn);

           
            cmd.Parameters.AddWithValue("@fn", this.firstn);
            cmd.Parameters.AddWithValue("@ln", this.lastn);
            cmd.Parameters.AddWithValue("@em", this.email);
            cmd.Parameters.AddWithValue("@un", this.username);
            cmd.Parameters.AddWithValue("@pwd", this.password);
            cmd.Parameters.AddWithValue("@dob", this.Dob);
            

            MySqlDataReader rdr = cmd.ExecuteReader();


            rdr.Close();
        }


        public override string ToString()
        {
            return $"{Firstn} {Lastn}";
        }
    }
}
