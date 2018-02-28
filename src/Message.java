public class Message
{

    private Integer maxUID, maxDist, roundNumber, sender, recipient; // initial values
    private String messageType = "default"; // since we implemented Peleg's without message type, need a default

	public Message(int maxUID, int maxDist, int roundNumber)
	{
		this.maxUID = maxUID;
		this.maxDist = maxDist;
		this.roundNumber = roundNumber;
	}

    public Message(String ServerMessage) {
        String[] splitMessage = ServerMessage.split(" ");

        // need to decide which type of message we are dealing with
        // this if check is here just to guard form cases where messageType may be empty
        // Talk about mess caused by bolting on stuff late. (¬_¬)
        String type = "";
        if (splitMessage.length >= 8) {
            type = splitMessage[7];
			this.messageType = type;
        }


        try {
            if (type.equals("") || type.equals("default")) {
                this.maxUID = Integer.parseInt(splitMessage[1]);
                this.maxDist = Integer.parseInt(splitMessage[3]);
                this.roundNumber = Integer.parseInt(splitMessage[5]);
            } else if (type.equals("search") || type.equals("parent") || type.equals("sync")) {
				this.maxUID = Integer.parseInt(splitMessage[1]);
				this.maxDist = Integer.parseInt(splitMessage[3]);
				this.roundNumber = Integer.parseInt(splitMessage[5]);
            } else {
                throw new Exception("Message type not known. Got " + messageType + " but can only accept \"\", \"default\", \"search\", \"parent\" and \"sync\"");
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while trying to construct message." + e.toString());
            System.exit(-1);
        }
    }

    /*
     * This message type is needed to implement BFS.
     * ==================
     * We will use 3 types of messages:
     *  - search: contains this node's ID
     *  - sync: used for synchronizing the nodes
     *  - parent: used to tell a process that it's the parent
     *
     * ==================
     * Fields:
     *  - roundNumber: the current round number at parent node
     *  - sender: UID of node who sent this message
     *  - recipient: contains the ID of node this message is intended for
     */
    public Message(String messageType, int maxUID, int maxDist, int roundNumber) {
        this.messageType = messageType;
		this.maxUID = maxUID;
		this.maxDist = maxDist;
		this.roundNumber = roundNumber;
    }

	public int getMaxUID()
	{
		return maxUID;
	}

	public int getMaxDist()
	{
		return maxDist;
	}

	public int getRoundNumber()
	{
		return roundNumber;
	}

	public void getMaxUID(int maxUID)
	{
		this.maxUID = maxUID;
	}

	public void getMaxDist(int maxDist)
	{
		this.maxDist = maxDist;
	}

	public void getRoundNumber(int roundNumber)
	{
		this.roundNumber = roundNumber;
	}

	public String getMessageType() {
		return this.messageType;
	}

    public String toString() {
        // messageType added, overloaded first two parameters to support BFS
        Integer maxIDOrSenderID, maxDistOrRecipientID;

        // by default, we will use maxUID and Distance. If they are -1, use SenderID or RecipientID
        if (maxUID == null) {
            maxIDOrSenderID = sender;
        } else {
            maxIDOrSenderID = maxUID;
        }

        if (maxDist == null) {
            maxDistOrRecipientID = recipient;
        } else {
            maxDistOrRecipientID = maxDist;
        }

        return "maxUID/sender: " + maxIDOrSenderID + " maxDist/recipient: " + maxDistOrRecipientID + " roundNumber: " + roundNumber + " messageType: " + messageType;
    }
}
