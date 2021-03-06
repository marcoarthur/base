package io.subutai.common.security.objects;


/**
 *
 */
public enum UserType
{
    SYSTEM( 1, "System" ), REGULAR( 2, "Regular" ), HUB( 3, "Hub" );

    private String name;
    private int id;


    UserType( int id, String name )
    {
        this.id = id;
        this.name = name;
    }


    public String getName()
    {
        return name;
    }


    public int getId()
    {
        return id;
    }

}
