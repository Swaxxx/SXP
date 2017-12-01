pragma solidity ^0.4.13;

contract Trade {
    struct Member {
        address add;
        string item;
        bool signed;
    }

    Member public member1;
    Member public member2;
    string public clauseA;
    string public clauseB;
    address public sender;

    function Trade(address add1, address add2, string item1, string item2, string clause1, string clause2) public {
        sender = msg.sender;
        require((msg.sender == sender) || (member1.add != add1));
        
        member1.add = add1;
        member1.item = item1;
        member2.add = add2;
        member2.item = item2;
        clauseA = clause1;  
        clauseB = clause2;
        
    }
    
    function getSender() public returns(address add) {
        return sender;
    }
    
    function sign() public {
        sender = msg.sender;
        if (member1.add == sender) {
            member1.signed = true;
        }
        else if (member2.add == sender) {
            member2.signed = true;
        }
        else {
            revert();
        }
    }
    
    function getAdd1() constant public returns(address add) {
        return member1.add;
    }
    
    function getAdd2() constant public returns(address add) {
        return member2.add;
    }
    
    function getItem1() constant public returns(string item) {
        return member1.item;
    }

    function getItem2() constant public returns(string item) {
        return member2.item;
    }
    
    function isSigned() constant public returns(bool signed) {
        if (sender == member1.add || sender == member2.add) {
            return (member1.signed && member2.signed);
        }
        else {
            revert();
        }
    }
    
    function getClauseA() constant public returns(string clause) {
        return clauseA;
    }
    
    function getClauseB() constant public returns(string clause) {
        return clauseB;
    }
}

