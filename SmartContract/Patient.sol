//SPDX-License-Identifier: GPL-3.0

pragma solidity >=0.5.0 <0.9.0;

contract Patient{
    struct record{
        string name;
        string location;
        string to;
        string from;
        string symptoms;
        string prescriptionUrls;
        string description;
        string privatekey;
    }

    mapping(string =>  mapping(string => record)) userRecords;

    function setNewPatientRecord(string memory serial, string memory email, string memory privatekey, string memory name, string memory location, string memory to, string memory from, string memory symptoms, string memory description, string memory prescriptionUrls) public {
        userRecords[email][serial] = record(name, location, to, from, symptoms, prescriptionUrls, description, privatekey);
    }
    function getPatientRecord(string memory serial, string memory email, string memory privatekey) public view returns(string memory, string memory, string memory, string memory, string memory, string memory, string memory){
        record memory newRecord = userRecords[email][serial];
        require(keccak256(abi.encodePacked(privatekey)) == keccak256(abi.encodePacked(newRecord.privatekey)), "Invalid privatekey");
        return (newRecord.name, newRecord.location, newRecord.to, newRecord.from, newRecord.symptoms, newRecord.prescriptionUrls, newRecord.description);
    }
}