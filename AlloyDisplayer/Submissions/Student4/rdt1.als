module rdt1
open util/ordering [RDTState]

sig Data {}

sig Packet {
	data: one Data
}

sig RDTState {
	senderBuffer: set Data,	
	receiverBuffer: set Data,	
	channel: lone Packet
}

fact packetCheck {
	all disj p, p': Packet |  (p.data != p'.data)
}

fun makePacket [d: Data]: set Packet {
	{p: Packet | p.data = d}
}

fun extractData [p:Packet]: Data {
	p.data
}

pred send [r,r': RDTState, d: Data]{
	one p: Packet |  p in makePacket[d] and 
		r'.channel = p and
		r'.senderBuffer = (r.senderBuffer - d) and
		r'.receiverBuffer = r.receiverBuffer
}

pred receive [r, r': RDTState]{
	let d = extractData[r.channel] | deliver [r, r', d]
}

pred deliver [r, r':RDTState, d: Data]{
	r'.senderBuffer = r.senderBuffer
	r'.receiverBuffer = r.receiverBuffer + d
	no r'.channel
}

pred Transition [r, r': RDTState] {
	(no r.channel) => (one d: r.senderBuffer | send [r,r',d]) 
	else
	receive [r, r']
}

pred Init [r: RDTState] {
	r.senderBuffer = Data 
    no r.receiverBuffer
	no r.channel
}

pred End[r: RDTState] {
	no r.senderBuffer
	r.receiverBuffer = Data
}

pred WorkingSimulation []{
	first.Init[]
	all r: RDTState - last | let r' = r.next | Transition [r, r']
	last.End[]
}

pred NotWorkingSimulation []{
	first.Init[]
	all r: RDTState - last | let r' = r.next | Transition [r, r']
	not last.End[]
}

run WorkingSimulation  for exactly 3 Data, exactly 3 Packet, exactly 7 RDTState
run NotWorkingSimulation  for exactly 3 Data, exactly 3 Packet, exactly 7 RDTState
