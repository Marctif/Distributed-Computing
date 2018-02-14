public class Message
{

	private int maxUID, maxDist, roundNumber;

	public Message(int maxUID, int maxDist, int roundNumber)
	{
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

	public String toString()
	{
		return "maxUID: " + maxUID + " maxDist: " + maxDist + " roundNumber: " + roundNumber;
	}


}