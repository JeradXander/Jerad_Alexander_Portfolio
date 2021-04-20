using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AlexanderJerad_FinalProject
{
    class NEO
    {
        //values
        private string name;
        private decimal diameter;
        private string url;
        private bool threat;
        private decimal mph;

        //properties
        public string Name { get => name; set => name = value; }
        public decimal Diameter { get => diameter; set => diameter = value; }
        public string Url { get => url; set => url = value; }
        public bool Threat { get => threat; set => threat = value; }
        public decimal Mph { get => mph; set => mph = value; }

        //ovveride
        public override string ToString()
        {
            return $"NEO name: {name} ";
        }
    }
}
