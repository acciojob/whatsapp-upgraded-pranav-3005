package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class WhatsappRepository {

    HashMap<String,User> userHashMap=new HashMap<>(); //pk-> num
    HashMap<String,Group> groupHashMap=new HashMap<>(); //pk-> name
    HashMap<String,List<User> > groupUsersMap=new HashMap<>(); //users of groups
    HashMap<String,Integer> groupMessageMap=new HashMap<>(); //no of msgs in a grp , (grp name,no of msg)
    HashMap<String,List<Message> > userMessageMap=new HashMap<>(); //msgs per user , (username,list(msg) )

    HashMap<Integer,Message> messageHashMap=new HashMap<>(); //pk -> cnt id (msgid,msg)
    int msgId=0;

    int count=0;




    public String addUser(String name, String mobile) throws Myexception {
        for(String i:userHashMap.keySet())
        {
            if(i.equals(mobile))
                throw new Myexception("User already exists");
        }
        userHashMap.put(mobile,new User(name,mobile));

        userMessageMap.put(name,new ArrayList<>());

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        int cnt= users.size();
        if(cnt==2)
        {
            String name=users.get(1).getName(); //2nd user
            groupHashMap.put(name,new Group(name,2));

            //add users to grp
            List<User>users1=new ArrayList<>();
            users1.add(users.get(0)); users1.add(users.get(1));
            groupUsersMap.put(name,users1);

            //add grp to grp,msg map
            groupMessageMap.put(name,0);

            return groupHashMap.get(name);


        }
        else
        {
            String name="Group "+ ++count;
            groupHashMap.put(name,new Group(name,cnt));

            //add users to grp
            List<User> users1=new ArrayList<>();
            for(User i:users)
                users1.add(i);
            groupUsersMap.put(name,users1);

            //add grp to grp,msg map
            groupMessageMap.put(name,0);

            return groupHashMap.get(name);
        }
    }

    public int createMessage(String content) {
        int id= ++msgId;
       // Date date=new Date();

        messageHashMap.put(id,new Message(id,content,new Date()));
        return id;
    }

    public int sendMessage(Message message, User sender, Group group) throws Myexception {
        for(String i: groupHashMap.keySet())
        {
            if(i.equals(group.getName()))
            {
                if(groupUsersMap.get(i).contains(sender))
                {
                    message.setId(++msgId);
                    message.setTimestamp(new Date());

                    messageHashMap.put(msgId,message);

                    groupMessageMap.put(i,groupMessageMap.get(i)+1);

                    userMessageMap.get(sender.getName()).add(message);

                    return groupMessageMap.get(i);
                }
                else
                {
                    throw new Myexception("You are not allowed to send message");
                }
            }
        }

        throw new Myexception("Group does not exist");

    }

    public String changeAdmin(User approver, User user, Group group) throws Myexception {

        if(!groupHashMap.containsKey(group.getName()))
        {
            throw new Myexception("Group does not exist");
        }

        if( ! groupUsersMap.get( group.getName() ).get(0).getName().equals( approver.getName() ) )
        {
            throw new Myexception("Approver does not have rights");
        }

        int userIndex=groupUsersMap.get(group.getName()).indexOf(user);
        groupUsersMap.get(group.getName()).set(0,user);
        groupUsersMap.get(group.getName()).set(userIndex,approver);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Myexception {
        for(String i :groupUsersMap.keySet())
        {
            if(groupUsersMap.get(i).contains(user))
            {
                if( groupUsersMap.get(i).get(0).getName().equals( user.getName() ) )
                {
                    throw new Myexception("Cannot remove admin");
                }
                else
                {
                    int totalMessages= userMessageMap.get(user.getName()).size();

                    //1
                    groupMessageMap.put( i , groupMessageMap.get(i) - totalMessages );

                    //2
                    for(Message msg: userMessageMap.get(user.getName()))
                    {
                        messageHashMap.remove(msg);
                    }
                    msgId = (messageHashMap.size()==0)?0:messageHashMap.size()-1;

                    //3
                    userMessageMap.remove(user.getName());

                    //4
                    userHashMap.remove(user.getName());

                    //5
                    groupUsersMap.get(i).remove(user);
                }
            }
        }

        throw new Myexception("User not found");
    }
}
