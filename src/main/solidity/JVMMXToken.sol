pragma solidity ^0.7.0;

import "./openzeppelin/ERC20.sol";
import "./openzeppelin/Ownable.sol";

contract JVMMXToken is ERC20, Ownable {

    constructor() ERC20("Java Mexico Token", "JVMMX Token") {
        _mint(msg.sender, 2000 ether);
    }

    function faucet(address to, uint256 amount) external onlyOwner {
        _mint(to, amount);
    }

    function burn(address account, uint256 amount) external onlyOwner {
        _burn(account, amount);
    }

}


