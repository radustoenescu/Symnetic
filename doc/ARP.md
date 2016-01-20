* CheckARPHeader()

Currently checks that the L2 EthProtocol Tag has the value for ARP and that the protocol requested in the ARP header is for IPv4 or IPv6. The click implementation also checks for the header size, but I don't see a way of doing this in SEFL.

* ARPQuerier(IP, MAC, IP, MAC,....)

Receives on input port 0 IP packets and sets their L2 DestHWAddr section based on a ARP table specified when creating the element. If the ip is found  in the table, the packet is sent to output 1. If there is no match then it creates a new ARP package (new L3 and L2) and sends it to out 0. It receives arp responses over input port 1 and repackages them back to ip packets it sends to outport 1. It is implemented using a recursive function that consumes pairs of two elements from the configParameters and generates nested If statements.

* ARPResponder(IP/MASK, MAC, IP/MASK, MAC, ....)

It recieves ARP requests over in port 0 and sends out arp replies over out port 1. If there is no match then it forwards the ARP request over out port 0. It is implemented using a recursive function that consumes pairs of two elements from the configParameters and generates nested If statements.

* ARPClassifier()

Sends ARP packets over port 0 and non-arp packets over port 1.

* Click files

    * ARP.click is a file that tests the interaction of ARPQuerier and ARPResponder
    * ARPReciever.click and ARPQuerier.click are files used in the development of each elements. Might be used for further testing.
    * CheckARPHeader.click is used for testing CheckARPHeader

* Test files

    * Arp.scala has all the tests.


