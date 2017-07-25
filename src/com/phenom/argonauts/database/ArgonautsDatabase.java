package com.phenom.argonauts.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.phenom.argonauts.Adventurer;
import com.phenom.argonauts.Main;
import com.phenom.argonauts.database.Error;
import com.phenom.argonauts.database.Errors;

/**
 * 
 * @author Evan Tellep
 * Made from template code provided by pablo67340
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
public abstract class ArgonautsDatabase 
{
    Connection connection; //Connection object to use in methods that communicate with the db.

    public abstract Connection getSQLConnection(); //Method charged with creating and establishing a connection with the database.

    public abstract void load(); //Charged with loading the database however the implementation sees fit.
    
    /**
     * Add a new record to tAdventurer
     * @param Name : Name of the Adventurer
     * @param UUID : Unique Identifier of the Adventurer (This doesn't change)
     * @param lastStyleID : The ID of the last style the adventurer played (default - 0)
     * @param homePointID : The ID of the players registered home point (default - 0)
     */
    public void addNewAdventurer(String Name, UUID UUID, int lastStyleID, int homePointID) 
    {
    	connection = getSQLConnection();
    	PreparedStatement addAdventurer = null;
    	try
    	{
    		addAdventurer = connection.prepareStatement("INSERT INTO tAdventurer (" + 
    				"UUID," +
    				"Name," +
    				"LastStyle," +
    				"HomePointID)" +
    				"VALUES (?, ?, ?, ?);");
    		addAdventurer.setString(1, UUID.toString());
    		addAdventurer.setString(2, Name);
    		addAdventurer.setDouble(3, lastStyleID);
    		addAdventurer.setDouble(4, homePointID);
    		addAdventurer.execute();
    	}
    	catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
            close(addAdventurer);
        }
    }
    
    /*
     * SELECT tAdventurer.Name, tHomePoint.xCoord, tHomePoint.yCoord, tHomePoint.zCoord, tRole.Name, tUnlockedRole.AbilityPoints, tUnlockedRole.Strength, tUnlockedRole.Vitality,
tUnlockedRole.Intelligence, tUnlockedRole.Wisdom, tStyle.Name, tUnlockedStyle.Attack, tUnlockedStyle.Defense, tUnlockedStyle.MagicAttack, tUnlockedStyle.MagicDefense, tUnlockedStyle.Health, 
tUnlockedStyle.Mana FROM tAdventurer
JOIN tHomePoint ON tHomePoint.HomePointID = tAdventurer.HomePointID
JOIN tStyle ON tStyle.Name = tAdventurer.LastStyle
JOIN tUnlockedStyle ON tUnlockedStyle.StyleID = tStyle.StyleID
JOIN tRole_Style ON tRole_Style.StyleID = tStyle.StyleID
JOIN tRole ON tRole.RoleID = tRole_Style.RoleID
JOIN tUnlockedRole ON tUnlockedRole.RoleID = tRole.RoleID
     */
    
    public void initAdventurer (Adventurer adventurer) {
    	connection = getSQLConnection();
    	PreparedStatement initAdventurer = null;
    	ResultSet response = null;
    	
    	try {
    		initAdventurer = connection.prepareStatement("SELECT tAdventurer.Name, tHomePoint.xCoord, tHomePoint.yCoord, tHomePoint.zCoord, tRole.Name, tUnlockedRole.AbilityPoints," +
					"tUnlockedRole.Strength, tUnlockedRole.Vitality, tUnlockedRole.Intelligence, tUnlockedRole.Wisdom, tStyle.Name, tUnlockedStyle.Attack, tUnlockedStyle.Defense," +
					"tUnlockedStyle.MagicAttack, tUnlockedStyle.MagicDefense, tUnlockedStyle.Health, tUnlockedStyle.Mana, tUnlockedStyle.Level, tUnlockedStyle.Exp FROM tAdventurer" +
					"JOIN tHomePoint ON tHomePoint.HomePointID = tAdventurer.HomePointID JOIN tStyle ON tStyle.Name = tAdventurer.LastStyle JOIN tUnlockedStyle ON tUnlockedStyle.StyleID = tStyle.StyleID JOIN tRole_Style ON tRole_Style.StyleID = tStyle.StyleID JOIN tRole ON tRole.RoleID = tRole_Style.RoleID JOIN tUnlockedRole ON tUnlockedRole.RoleID = tRole.RoleID " +
					"WHERE tAdventurer.Name = '" + adventurer.getPlayer().getName() + "'");
    		response = initAdventurer.executeQuery();
    		//Looks like I'll need to modify the tHomePoint Schema to allow for a World to be set
    		adventurer.setHome(new Location(Bukkit.getWorld("StartingIsland"), response.getDouble(2), response.getDouble(3), response.getDouble(4)));
    		//Might need to grab column 5 : Role name if ricky wants me to add a LastRole property to adventurer for now
    		adventurer.setAbilityPoints(response.getInt(6));
    		adventurer.setStr(response.getInt(7));
    		adventurer.setVit(response.getInt(8));
    		adventurer.setIntel(response.getInt(9));
    		adventurer.setWis(response.getInt(10));
    		//Might need to grab column 11 : Style Name if ricky wants me to add a LastStyle property to adventurer for now
    		adventurer.setAtk(response.getDouble(12));
    		adventurer.setDef(response.getDouble(13));
    		adventurer.setMagAtk(response.getDouble(14));
    		adventurer.setMagDef(response.getDouble(15));
    		adventurer.setHp(response.getDouble(16));
    		adventurer.setMp(response.getDouble(17));
    		adventurer.setLvl(response.getInt(18));
    		adventurer.setExp(response.getInt(19));
    	}
    	catch (SQLException ex) {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    	finally {
            close(initAdventurer, response);
        }
    	
    }
    
    /**
     * Initialize the values for the Adventurer (stats, level, etc.)
     * @param adventurer : The adventurer to initialize
     */
    /*public void initAdventurer(Adventurer adventurer) {
    	connection = getSQLConnection();
    	PreparedStatement initAdventurer = null;
    	ResultSet response = null;
    	double[] stats = new double[6];
    	String uuid = "";
    	int lastStyleID = 0;
    	int homepointID = 0;
    	int[] homePoint = new int[3];
    	int styleID = 0;
    	int adventurerID = 0;
    	
    	//Handling initialization of variables within tAdventurer
    	try 
    	{
    		initAdventurer = connection.prepareStatement("SELECT * FROM tAdventurer WHERE Name = '" + adventurer.getPlayer().getName() + "';");
    		response = initAdventurer.executeQuery();
    		adventurerID = response.getInt(1);
    		uuid = response.getString(2);
    		lastStyleID = response.getInt(4);
    		homepointID = response.getInt(11);
    	}
    	catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
            close(initAdventurer, response);
        }
    	
    	//Handling initialization of HomePoint
    	try 
    	{
    		initAdventurer = connection.prepareStatement("SELECT * FROM tHomePoint WHERE HomePointID = " + homepointID + ";");
    		response = initAdventurer.executeQuery();
    		homePoint[0] = response.getInt(1);
    		homePoint[1] = response.getInt(2);
    		homePoint[2] = response.getInt(3);
    	}
    	catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
        	close(initAdventurer, response);
        }

    	//Handling logging in as the last played style
    	try 
    	{
    		initAdventurer = connection.prepareStatement("SELECT * FROM tUnlockedStyle WHERE UnlockedStyleID = " + lastStyleID + " AND AdventurerID = " + adventurerID + ";");
    		response = initAdventurer.executeQuery();
    		for (int i = 0; i < stats.length; i++)
    		{
    			stats[i] = response.getDouble(i + 2); //Add 2 as an offset to i to only grab the stat values which are doubles
    		}
    		styleID = response.getInt(1);
    	}
    	catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
        	close(initAdventurer, response);
        }
    	adventurer.setAtk(stats[0]);
    	adventurer.setDef(stats[1]);
    	adventurer.setMagAtk(stats[2]);
    	adventurer.setMagDef(stats[3]);
    	adventurer.setHp(stats[4]);
    	adventurer.setMp(stats[5]);
    	adventurer.setUuid(UUID.fromString(uuid));
    }*/
    
    public void saveAdventurer (Adventurer adventurer) {
    	connection = getSQLConnection();
    	PreparedStatement saveAdventurer = null;
    	
    	
    }
    
    /**
     * Checks to see if the player exists in the database.
     * @param playerName : The Name of the player.
     * @param UUID : The Unique ID of the player.
     * @return : True if a record is returned based on the Name and Unique ID of the player; False otherwise.
     */
    public boolean checkForAdventurer(String playerName, UUID UUID)
    {
        connection = getSQLConnection();
        PreparedStatement checkForPlayer = null;
        ResultSet rs = null;
        try
        {
        	checkForPlayer = connection.prepareStatement("SELECT * FROM tAdventurer WHERE Name = ? AND UUID = ?");
        	checkForPlayer.setString(1, playerName);
        	checkForPlayer.setString(2, UUID.toString());
            rs = checkForPlayer.executeQuery();
        } 
        catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
            try 
            {
            	if (rs.isBeforeFirst()) 
            	{
            		close(checkForPlayer, rs);
            		return true;
            	}
                if (connection != null)
                {
                	connection.close();                	
                }
            } 
            catch (SQLException ex) 
            {
                Main.plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return false;
    }
    
    /**
     * Checks to see if the player exists in the database.
     * @param playerName : The Name of the player
     * @return : True if a record is returned based on the Name and Unique ID of the player; False otherwise.
     */
    public boolean checkForAdventurer(String playerName)
    {
        connection = getSQLConnection();
        PreparedStatement checkForPlayer = null;
        ResultSet rs = null;
        try
        {
        	checkForPlayer = connection.prepareStatement("SELECT * FROM tAdventurer WHERE Name = ?");
        	checkForPlayer.setString(1, playerName);
            rs = checkForPlayer.executeQuery();
        } 
        catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
            try 
            {
            	if (rs.isBeforeFirst()) 
            	{
            		close(checkForPlayer, rs);
            		return true;
            	}
                if (connection != null)
                {
                	connection.close();                	
                }
            } 
            catch (SQLException ex) 
            {
                Main.plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return false;
    }
    
    /**
     * Checks to see if the player exists in the database.
     * @param UUID : The Unique ID of the player
     * @return : True if a record is returned based on the Name and Unique ID of the player; False otherwise.
     */
    public boolean checkForAdventurer(UUID UUID)
    {
        connection = getSQLConnection();
        PreparedStatement checkForPlayer = null;
        ResultSet rs = null;
        try
        {
        	checkForPlayer = connection.prepareStatement("SELECT * FROM tAdventurer WHERE UUID = ?");
        	checkForPlayer.setString(1, UUID.toString());
            rs = checkForPlayer.executeQuery();
        } 
        catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
        finally 
        {
            try 
            {
            	if (rs.isBeforeFirst()) 
            	{
            		close(checkForPlayer, rs);
            		return true;
            	}
                if (connection != null)
                {
                	connection.close();                	
                }
            } 
            catch (SQLException ex) 
            {
                Main.plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return false;
    }
    
    public String getUUID (String name) {
    	connection = getSQLConnection();
    	PreparedStatement getUUID = null;
    	ResultSet response = null;
    	String result = "";
    	
    	try {
    		getUUID = connection.prepareStatement("SELECT UUID FROM tAdventurer WHERE Name = '" + name + "';");
    		response = getUUID.executeQuery();
    		result = response.getString(1);
    	}
    	catch (SQLException ex) {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    	finally {
    		close(getUUID);
    	}
    	return result;
    }
    
    /**
     * Update a players name in the database since MC now supports name changing
     * @param uuid : A players Unique Identifier that never changes
     * @param newName : The players new name
     */
    public void updateName (String uuid, String newName) {
    	connection = getSQLConnection();
    	PreparedStatement updateName = null;
    	try {
    		updateName = connection.prepareStatement("UPDATE tAdventurer SET " + 
    				"Name = (?)" +
    				"WHERE UUID = '" + uuid + "';");
        	updateName.setString(1, newName);
        	updateName.execute();
        	
    	}
    	catch (SQLException ex) 
        {
            Main.plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    	finally {
    		close(updateName);
    	}
    }
    
    /**
     * A convenience method to close a prepared statement and a result set
     * @param ps : The Prepared Statement to close
     * @param rs : The Result Set to close
     */
    public void close(PreparedStatement ps,ResultSet rs)
    {
    	try 
    	{
    		if (ps != null)
    		{
    			ps.close();            	
    		}
    		if (rs != null)
    		{            	
    			rs.close();
    		}
    		if (connection != null)
    		{
    			connection.close();
    		}
    	} 
    	catch (SQLException ex) 
    	{
    		Error.close(Main.plugin, ex);
    	}
    }

    /**
     * A convenience method to close a prepared statement and a result set
     * @param ps : The Prepared Statement to close
     */
    public void close(PreparedStatement ps)
    {
    	try 
    	{
    		if (ps != null)
    		{
    			ps.close();            	
    		}
    		if (connection != null)
    		{
    			connection.close();
    		}
    	} 
    	catch (SQLException ex) 
    	{
    		Error.close(Main.plugin, ex);
    	}
    }
    
	//Leaving this here for example purposes for now
    public void setTokens(Player player, Integer tokens, Integer total) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO tTest (player,kills,total) VALUES(?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, player.getName().toLowerCase());                                             // YOU MUST put these into this line!! And depending on how many
                                                                                                         // colums you put (say you made 5) All 5 need to be in the brackets
                                                                                                         // Seperated with comma's (,) AND there needs to be the same amount of
                                                                                                         // question marks in the VALUES brackets. Right now i only have 3 colums
                                                                                                         // So VALUES (?,?,?) If you had 5 colums VALUES(?,?,?,?,?)
                                                                                                    
            ps.setInt(2, tokens); // This sets the value in the database. The colums go in order. Player is ID 1, kills is ID 2, Total would be 3 and so on. you can use
                                  // setInt, setString and so on. tokens and total are just variables sent in, You can manually send values in as well. p.setInt(2, 10) <-
                                  // This would set the players kills instantly to 10. Sorry about the variable names, It sets their kills to 10 i just have the variable called
                                  // Tokens from another plugin :/
            ps.setInt(3, total);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            Main.plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Main.plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;           
    }
}
