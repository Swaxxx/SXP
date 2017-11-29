pragma solidity ^0.4.18;

contract Trade {
    struct Member {
        address add;
        string item;
        bool signed;
    }

	Member [] public members;
	string [] public clauses;
    address public sender;
	uint number;
	uint clauses_number;

    function Trade(address [] addresses, string [] items, uint _number, string [] _clauses, uint _clauses_number) public {
        sender = msg.sender;
        
		for (uint i = 0; i < _number; i++) {
			members.push(Member(addresses[i], items[i], false));
		}
        
		for (uint j = 0; j < _clauses_number; j++) {
			clauses[i] = _clauses[i];
		}
    }
    
    function getSender() public returns(address add) {
        return sender;
    }
    
    function sign() public returns(bool) {
        sender = msg.sender;
		for (uint i = 0; i < number; i++) {
			if (members[i].add == sender) {
				members[i].signed = true;
				return true;
			}
		}
        
		revert();
    }
    
    function getAdd(uint i) constant public returns(address add) {
        return members[i].add;
    }
    
    function getItem(uint i) constant public returns(string item) {
        return members[i].item;
    }
    
    function isSigned() constant public returns(bool signed) {
		for (uint i = 0; i < number; i++) {
			if (members[i].add == sender) {
				uint j = 0;
				while (true) {
					if (!members[j].signed) {
						return false;
					}
					j++;
				}
			}
		}
	
        revert();
    }
	
	function getClause(uint i) constant public returns(string clause) {
		return clauses[i];
	}
}
