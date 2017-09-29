These lists are saved in the form `[prefix]-[lamda].txt'

The structure is:
IPADDRESS,DWELL-TIME
IPADDRESS,DWELL-TIME
IPADDRESS,DWELL-TIME
IPADDRESS,DWELL-TIME
IPADDRESS,DWELL-TIME
...
IPADDRESS,DWELL-TIME

DWELL-TIME is the duration to use the current IP address in seconds.

Say you wanted to use the prefix 184.164.241.0/24 with a lamda value of 0.25.  Then, you could simply read from the list `184.164.241.0-0.25.txt'

If you need to generate a list for a different prefix, a different lamda, or have more elements in the list, then you can generate a list with the attached python code.

$ ./gen-iplist.py ip_prefix lamda num_ips

Parameters
--------------
ip_prefix: The desired IP prefix in CIDR notation (e.g. 184.164.241.0/24)
lamda: The scale parameter for the exponential RV.  See the following image for example values: https://en.wikipedia.org/wiki/Exponential_distribution#/media/File:Exponential_pdf.svg
(When referring the to graph on wikipedia, the x axis is seconds for our purposes)
num_ips: How many IPs should be in the list.
