## About MicroRaiden Java
Eventually the goal is a full port of the [µRaiden](https://github.com/raiden-network/microraiden) project from
Python to Java. However, initially - we're aiming for the bare min
functionality to support micropayment channels between two peers such
that we can automatically deploy channels from one entity with some
initial deposit supplied by the initiator of the channel. 
The current version of code has been tested with [Kovan](https://gitter.im/kovan-testnet/) testnet only for now. However, other testnet should also work after proper configurations.
## Prerequisites and Notes
- Parity installation. More information about parity intallation can be found https://www.parity.io/. The version of parity being used is 1.8.4-beta. 
- Run your parity service with the following command:
```
parity --geth --chain kovan --force-ui --reseal-min-period 0 --jsonrpc-cors "*" --jsonrpc-apis web3,eth,net,parity,traces,rpc,personal
```
It may take a while to get syncronized.
## Working with the existing contracts deployed on Kovan.
We have deployed the [CustomToken](https://kovan.etherscan.io/address/0x0fc373426c87f555715e6fe673b07fe9e7f0e6e7) and [RaidenMicroTransferChannels](https://kovan.etherscan.io/address/0x5832edf9Da129Aa13fdA0fBff93379d3ED8a4a93) contracts. The ABI and addresses have been added in the configuration file `rm-ethereum.conf` in this project. 
### Building
```
./gradlew installDist
```
### Running
```
./gradlew run
``` or 
```
./build/install/microraiden-java/bin/microraiden-java
```
### Use createAccount command to create an account
```
./build/install/microraiden-java/bin/microraiden-java createAccount Alice
```
This version of the project now is very basic. We do not take any credentials from you when creating the account. A file Alice.pkey should be created with private key shown in plaintext. 
### Use getAccountInfo command to get the information of the new account
```
./build/install/microraiden-java/bin/microraiden-java getAccountInfo Alice
```
If your parity service running correctly, you should see your AccountName, AccountID, AccountNonce, and AccountBalance.
### Get some free Kovan test Ethers
Copy the AccountID and paste it [here](https://gitter.im/kovan-testnet/faucet). Wait a period of time until the test ethers have been given to your account.
### Get some Custom token by using buyToken command
```
./build/install/microraiden-java/bin/microraiden-java buyToken Alice 0.1
```
### Use getTokenBalance command to see the number of tokens in the account
```
./build/install/microraiden-java/bin/microraiden-java getTokenBalance Alice
```
### Create another new account as the payment recipient.
```
./build/install/microraiden-java/bin/microraiden-java createAccount Bob
```
The token balance of Bob should be zero.
### Create a payment channel from Alice to Bob by using createChannel command
```
./build/install/microraiden-java/bin/microraiden-java createChannel Alice Bob 30
```
Wait until the transaction has been mined.
A channel key, a block number, and a URL should be shown in the terminal. The key can be used to see the created channel by pasting it into the `channels` field in the URL page. 
The token balance of Alice should be updated now since she puts 30 Tokens into the payment channel as deposit. However, Bob cannot receive it since the channel has not been closed.
### Close the payment channel and check the balances of Alice and Bob
```
./build/install/microraiden-java/bin/microraiden-java closeChannelCooperatively Alice Alice Bob 5351492 12.5
```
The first `Alice` is the delegator who send the transcation to Kovan testnet. The second `Alice` and `Bob` are the token sender and receiver, respectively. `5351492` is the block number where the payment channel was created. `12.5` is the real amount of token that Alice eventually pays Bob.
The token balances of Alice and Bob should be updated.
### Working with RightMesh library.
All of these functions should work by signing transactions and relaying
them into a superpeer. For now we'll just set the superpeer ip address
but when integrated into RM, this part will be replaced by relaying it
through the mesh to the superpeer.

## Working with your own token and develop a new channel contract
- It is very important to noted that the ethererumj currently DOES NOT support the newest version of Solidity (0.4.19). Some of the new features in Solidity with version >= 0.4.16, such as key words `view` and `pure`, are not compatible when using ethereumj to parse the application binary interface (ABI) of the smart contract. In order to install a paticular version solidity compiler (e.g. 0.4.15), please use `brew` command as shown [here](http://solidity.readthedocs.io/en/develop/installing-solidity.html#binary-packages).
- The smart contracts used in this project are not exactly the same as the smart contracts in µRaiden project. The smart contracts compatible with this project have been included in this repo already.
- Users can use `populus` to compile and deploy them onto the testnet. Referring the steps and configurations [here](https://github.com/raiden-network/microraiden/blob/master/contracts/README.md#usage) is helpful.
- After the smart contract deployment onto Kovan testnet, open file rm-ethereum.conf and update the address values. 
- The updated configuration now is ready to work with this project. 
- In order to see payment channels opened at your deployed smart contracts on Etherscan, you need to manusally assemble all pieces of solidity files and perform the solidity contract source code verification [here](https://etherscan.io/verifyContract).
