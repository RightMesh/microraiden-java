pragma solidity ^0.4.15;

library ECVerify {

    function ecverify(bytes32 hash, bytes32 r, bytes32 s, uint8 v) internal constant returns (address signature_address) {

        // Version of signature should be 27 or 28, but 0 and 1 are also possible
        if (v < 27) {
            v += 27;
        }

        require(v == 27 || v == 28);
        signature_address = ecrecover(hash, v, r, s);

        // ecrecover returns zero on error
        require(signature_address != 0x0);

        return signature_address;
    }
}
