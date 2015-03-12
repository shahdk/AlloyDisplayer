module rdt22
open util/ordering [RDTState]
open util/ordering [Packet]
open util/ordering [CheckSum]
open util/ordering [Data]
open util/ordering [ACK]
open util/ordering [S0]
open util/ordering [S1]

//===================================================================================================================

abstract sig SeqNum {}
one sig S0, S1 extends SeqNum {}

sig CheckSum {}

abstract sig Payload {checkSum: one CheckSum}
sig ACK, Data extends Payload {}

sig Packet {
	data: one Payload,
	checkSum: one CheckSum,
	dataSequence: one SeqNum
}

sig RDTState {
	senderBuffer: set Data,	
	receiverBuffer: set Data,	
	channel: lone Packet,
 	currentData: lone Data,
	senderSequence: lone SeqNum,
	receiverSequence: lone SeqNum
}

//===================================================================================================================

fact orderingPacketAndACK {
	all d, d': ACK |
		let p = d.~data |
			let p' = d'.~data |
				lt[p, p'] => lte[d, d']
}

fact orderingPacketAndCheckSum {
	all disj p, p': Packet |
		let c = p.checkSum | 
			let c' =  p'.checkSum |
				lt[p, p'] => lte[c, c']
}

fact orderingPacketAndData {
	all d, d': Data |
		let p = d.~data |
			let p' = d'.~data |
				lt[p, p'] => lte[d, d']
}

fact orderingStateAndPacket {
	all disj p, p': Packet |
		let r = p.~channel | 
			let r' =  p'.~channel |
				lt[p, p'] => lt[r, r']
}

//===================================================================================================================

pred allValidPackets {
	all p: Packet | p.checkSum = p.data.checkSum
}

pred doNotRepeatACKNAKPackets {
	all disj r, r': RDTState | 
		(r.channel.data not in Data) => (r.channel != r'.channel)
}

pred isValidSequence [r: RDTState] {
	r.senderSequence = r.channel.dataSequence
}

pred isValidPacket [p: Packet] {
	p.checkSum = p.data.checkSum
}

fact distinctCheckSum {
	all disj d, d': Payload | (d.checkSum != d'.checkSum)
}

fact dataMappedToAtleastOnePacket {
	all d: Payload | some p : Packet | d = p.data
}

fact ensureDistinctPackets {
	all disj p, p': Packet |  (p.data = p'.data) => (p.checkSum != p'.checkSum)
}

//===================================================================================================================

pred oneCorruptedData {
	one p:Packet |  
		p.data in Data and 
		p.checkSum != p.data.checkSum and
		(all p': Packet - p | p'.checkSum = p'.data.checkSum)
}

pred twoCorruptedData {
	some p:Packet, p': Packet-p |  
		p.data in Data and 
		p.checkSum != p.data.checkSum and
		p'.data in Data and 
		p'.checkSum != p'.data.checkSum and
		(all p'': Packet - p - p' | p''.checkSum = p''.data.checkSum)
}

pred oneCorruptedACK {
	one p:Packet |  
		p.data in ACK and 
		p.checkSum != p.data.checkSum and
		(all p': Packet - p | p'.checkSum = p'.data.checkSum)
}

pred oneCorruptedDataAndCorruptedACK {
	some p:Packet, p': Packet-p |  
		p.data in ACK and 
		p.checkSum != p.data.checkSum and
		p'.data in Data and 
		p'.checkSum != p'.data.checkSum and
		(all p'': Packet - p - p' | p''.checkSum = p''.data.checkSum)
}

//===================================================================================================================

fun extractPacketData [p: Packet]: Data {
	p.data
}

fun extractStateData [r: RDTState]: Data {
	r.currentData
}

fun makePacket [d: Payload]: set Packet {
	{
		p: Packet | p.data = d  
	}
}

//===================================================================================================================

pred send [r,r': RDTState, d: Data] {
	one p: Packet | p in makePacket[d] and 
		r'.channel = p and
		r'.senderBuffer = (r.senderBuffer - d) and
		r'.receiverBuffer = r.receiverBuffer and
		r'.receiverSequence = r.receiverSequence and
		r'.currentData = d and
		(
			(no r.senderSequence) => 
				((r'.senderSequence = S0) and (p.dataSequence = S0))
			else
			((r'.senderSequence = SeqNum - r.senderSequence) and
			 (p.dataSequence = SeqNum - r.senderSequence))
		)
}

pred resendValid [r,r': RDTState, d: Data] {
	one p: Packet | p in makePacket[d] and 
		p.checkSum = d.checkSum and
		r'.channel = p and
		r'.senderBuffer = (r.senderBuffer - d) and
		r'.receiverBuffer = r.receiverBuffer and
		r'.receiverSequence = r.receiverSequence and
		r'.currentData = d and
		r'.senderSequence = r.senderSequence and
		p.dataSequence = r.senderSequence
}

pred resend [r,r': RDTState, d: Data] {
	one p: Packet | p in makePacket[d] and
		r'.channel = p and
		r'.senderBuffer = (r.senderBuffer - d) and
		r'.receiverBuffer = r.receiverBuffer and
		r'.receiverSequence = r.receiverSequence and
		r'.currentData = d and
		r'.senderSequence = r.senderSequence and
		p.dataSequence = r.senderSequence
}

//===================================================================================================================

pred sendACKPacket [r, r': RDTState] {
	(one a: ACK, p: Packet | p in makePacket[a] and r'.channel = p and p.dataSequence = r.channel.dataSequence)
}

pred sendValidACKPacket [r': RDTState] {
	(one a: ACK, p: Packet | p in makePacket[a] and r'.channel = p and p.checkSum = a.checkSum)
}

pred sendNAKPacket [r, r': RDTState] {
	(one n: ACK, p: Packet | p in makePacket[n] and r'.channel = p and p.dataSequence = SeqNum - r.channel.dataSequence)
}

pred receive [r, r': RDTState] {
	let d = extractPacketData[r.channel] |
		((r.channel.checkSum = d.checkSum) and (r.channel.dataSequence != r.receiverSequence)) => 
			deliver [r, r', d]
		else
		((r.channel.checkSum = d.checkSum) and (r.channel.dataSequence = r.receiverSequence)) => 
			doNotDeliver [r, r', d] and sendValidACKPacket [r']
		else
		doNotDeliver [r, r', d] and sendNAKPacket [r, r']
}

//===================================================================================================================

pred doNotDeliver [r, r':RDTState, d: Data]{
	r'.senderBuffer = r.senderBuffer 
	r'.receiverBuffer = r.receiverBuffer
	r'.currentData = r.currentData
	r'.senderSequence = r.senderSequence
	r'.receiverSequence = r.receiverSequence
}

pred deliver [r, r':RDTState, d: Data]{
	r'.senderBuffer = r.senderBuffer
	r'.receiverBuffer = r.receiverBuffer + d
	r'.senderSequence = r.senderSequence
	r'.receiverSequence = r.channel.dataSequence
	r'.currentData = r.currentData
	sendACKPacket [r, r']
}

//===================================================================================================================

pred Init [r: RDTState] {
	r.senderBuffer = Data 
   	no r.receiverBuffer
	no r.channel
	no r.currentData
	no r.senderSequence
	no r.receiverSequence
}

pred Transition [r, r': RDTState] {
	(no r.channel) => (one d: r.senderBuffer | send [r,r',d])

	else
	((r.channel.data in ACK) and isValidPacket [r.channel] and isValidSequence [r]) => 
		(
			(no r.senderBuffer) => (endTransmission [r, r'])
		 	else
			(one d: r.senderBuffer | send [r,r',d])
		)

	else
	((r.channel.data in ACK) and ((not isValidPacket [r.channel]) or (not isValidSequence [r]))) => 
		(let d = extractStateData[r] | 
			(not resendValid [r, r', d]) => resend [r,r',d]
			else
			resendValid [r, r', d])

	else
	(r.channel.data in Data) => receive [r, r']
}

pred endTransmission [r, r': RDTState] {
	r'.senderBuffer = r.senderBuffer 
	r'.receiverBuffer = r.receiverBuffer
	no r'.channel
	no r'.currentData
	no r'.senderSequence
	no r'.receiverSequence
}

pred End[r: RDTState] {
	no r.senderBuffer
	no r.channel
	no r.currentData
	no r.senderSequence
	no r.receiverSequence
	r.receiverBuffer = Data
}

//===================================================================================================================

pred WorkingSimulationWithoutNAK [] {
	doNotRepeatACKNAKPackets
	allValidPackets
	first.Init []
	all r: RDTState - last | let r' = r.next | Transition [r, r'] 
	last.End []
}

run WorkingSimulationWithoutNAK  for exactly 3 Data, 
									 exactly 3 ACK, 
									 exactly 6 Packet, 
									 exactly 6 CheckSum, 
									 exactly 8 RDTState

//===================================================================================================================

pred WorkingSimulationWithNAK []{
	first.Init[]
	doNotRepeatACKNAKPackets
	oneCorruptedData
	all r: RDTState - last | let r' = r.next | Transition[r, r'] 
	last.End[]
}

run WorkingSimulationWithNAK  for exactly 3 Data, 
								  exactly 4 ACK,   
								  exactly 8 Packet, 
								  exactly 7 CheckSum, 
								  exactly 10 RDTState

//===================================================================================================================

pred WorkingSimulationWithTwoNAK [] {
	doNotRepeatACKNAKPackets
	twoCorruptedData
	first.Init []
	all r: RDTState - last | let r' = r.next | Transition[r, r'] 
	last.End []
}

run WorkingSimulationWithTwoNAK  for exactly 3 Data, 
									 exactly 5 ACK,  
									 exactly 10 Packet, 
									 exactly 8 CheckSum,
									 exactly 12 RDTState

//===================================================================================================================

pred WorkingSimulationWithCorruptedACK [] {
	doNotRepeatACKNAKPackets
	oneCorruptedACK
	first.Init []
	all r: RDTState - last | let r' = r.next | Transition [r, r']
	last.End[]
}

run WorkingSimulationWithCorruptedACK  for exactly 3 Data, 
										   exactly 3 ACK, 
										   exactly 7 Packet, 
										   exactly 6 CheckSum,
										   exactly 10 RDTState

//===================================================================================================================

pred sendFirstPacket [r,r': RDTState, d: Data] {
	one p: Packet | p in makePacket[d] and 
		r'.channel = p and
		p.checkSum != d.checkSum and
		r'.senderBuffer = (r.senderBuffer - d) and
		r'.receiverBuffer = r.receiverBuffer and
		r'.receiverSequence = r.receiverSequence and
		r'.currentData = d and
		(
			(no r.senderSequence) => 
				((r'.senderSequence = S0) and (p.dataSequence = S0))
			else
			((r'.senderSequence = SeqNum - r.senderSequence) and
			 (p.dataSequence = SeqNum - r.senderSequence))
		)
}

pred WorkingSimulationWithNAKAndCorruptedACK []{
	doNotRepeatACKNAKPackets
	oneCorruptedDataAndCorruptedACK
	first.Init []
	(one d: first.senderBuffer | sendFirstPacket [first, first.next, d])
	all r: RDTState - last | let r' = r.next | Transition [r, r'] 
	last.End []
}

run WorkingSimulationWithNAKAndCorruptedACK  for exactly 3 Data, 
												 exactly 4 ACK,  
												 exactly 9 Packet, 
												 exactly 7 CheckSum, 
												 exactly 12 RDTState

//===================================================================================================================

pred NotWorkingSimulationWithNAK []{
	oneCorruptedData
	first.Init []
	all r: RDTState - last | let r' = r.next | Transition [r, r'] 
	not last.End []
}

run NotWorkingSimulationWithNAK  for exactly 3 Data, 
						             exactly 4 ACK,   
						             exactly 7 Packet, 
						             exactly 7 CheckSum, 
						             exactly 10 RDTState
