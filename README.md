## About MicroRaiden Java
Eventually the goal is a full port of the micro raiden project from
python to java. However, initially - we're aiming for the bare min
functionality to support micropayment channels between two peers such
that we can automatically deploy channels from one entity with some
initial balance supplied by the initiator of the channel.

It should be possbile to shift the available balance in either direction.

- updateBalance(frompeer, topeer, amount)

The channel should be able to be closed (co-operatively, and then
eventually un-cooperatively.)

- closeChannel(remotePeer)

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
