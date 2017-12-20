## About MicroRaiden Java
Eventually the goal is a full port of the micro raiden project from
python to java. However, initially - we're aiming for the bare min
functionality to support micropayment channels between two peers such
that we can automatically deploy channels from one entity with some
initial balance supplied by the initiator of the channel.

- createChannel(remotePeer, initialBalance) - called by the channel initiator

It should be possbile to shift the available balance in either direction.

- updateBalance(frompeer, topeer, amount) - called by either the channel 
initator or the remote peer should generate a signed transaction by the
local peer. Should use the local private key to generate the signed 
transaction. Should not broadcast the balance to the blockchain. It should
probably append the signed transaction into a local file so that we can
use the file to simulate the list of transactions from the remote peer
and then when the channel is closed broadcast the latest transaction.

The channel should be able to be closed (co-operatively, and then
eventually un-cooperatively.)

- closeChannel(remotePeer, closingTransaction) - at first this should just
broadcast the latest signed transaction from the remotePeer. Eventually
we should be able to select which transaction exactly, so that we can
test malicious / non-cooperative cases.

It might be good to have a get balance function that shows:

1. channel balance (tokens)
2. account balance (tokens + eth not in the channel)
3. total balance (tokens in account + token in channel + eth)

All of these functions should work by signing transactions and relaying
them into a superpeer. For now we'll just set the superpeer ip address
but when integrated into RM, this part will be replaced by relaying it
through the mesh to the superpeer.

## Building
`./gradlew installDist`

## Running
`./gradlew run` or `./build/install/microraiden-java/bin/microraiden-java`

## Running a specific test function
`./build/install/microraiden-java/bin/microraiden-java <function> <args>`
