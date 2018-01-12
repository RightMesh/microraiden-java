pragma solidity ^0.4.15;

/// @title Base Token contract - Functions to be implemented by token contracts.
contract Token {
    /*
     * Implements ERC 20 standard.
     * https://github.com/ethereum/EIPs/blob/f90864a3d2b2b45c4decf95efd26b3f0c276051a/EIPS/eip-20-token-standard.md
     * https://github.com/ethereum/EIPs/issues/20
     *
     *  Added support for the ERC 223 "tokenFallback" method in a "transfer" function with a payload.
     *  https://github.com/ethereum/EIPs/issues/223
     */

    /*
     * This is a slight change to the ERC20 base standard.
     * function totalSupply() constant returns (uint256 supply);
     * is replaced with:
     * uint256 public totalSupply;
     * This automatically creates a getter function for the totalSupply.
     * This is moved to the base contract since public getter functions are not
     * currently recognised as an implementation of the matching abstract
     * function by the compiler.
     */
    uint256 public totalSupply;

    /*
     * NOTE:
     * The following variables were optional. Now, they are included in ERC 223 interface.
     * They allow one to customise the token contract & in no way influences the core functionality.
     */
    string public name;                   //fancy name: eg Simon Bucks
    uint8 public decimals;                //How many decimals to show. ie. There could 1000 base units with 3 decimals. Meaning 0.980 SBX = 980 base units. It's like comparing 1 wei to 1 ether.
    string public symbol;                 //An identifier: eg SBX


    /// @param _owner The address from which the balance will be retrieved.
    /// @return The balance.
    function balanceOf(address _owner) public constant returns (uint256 balance);

    /// @notice send `_value` token to `_to` from `msg.sender`.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @param _data Data to be sent to `tokenFallback.
    /// @return Returns success of function call.
    function transfer(address _to, uint256 _value, bytes _data) public returns (bool success);

    /// @notice send `_value` token to `_to` from `msg.sender`.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @return Whether the transfer was successful or not.
    function transfer(address _to, uint256 _value) public returns (bool success);

    /// @notice send `_value` token to `_to` from `_from` on the condition it is approved by `_from`.
    /// @param _from The address of the sender.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @return Whether the transfer was successful or not.
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success);

    /// @notice `msg.sender` approves `_spender` to spend `_value` tokens.
    /// @param _spender The address of the account able to transfer the tokens.
    /// @param _value The amount of tokens to be approved for transfer.
    /// @return Whether the approval was successful or not.
    function approve(address _spender, uint256 _value) public returns (bool success);

    /// @param _owner The address of the account owning tokens.
    /// @param _spender The address of the account able to transfer the tokens.
    /// @return Amount of remaining tokens allowed to spent.
    function allowance(address _owner, address _spender) public constant returns (uint256 remaining);

    /*
     * Events
     */
    event Transfer(address indexed _from, address indexed _to, uint256 _value);
    event Approval(address indexed _owner, address indexed _spender, uint256 _value);

    // There is no ERC223 compatible Transfer event, with `_data` included.
}
