package io.subutai.common.settings;


import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Created by ermek on 2/19/16.
 */
public class SystemSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemSettings.class );

    public static final String DEFAULT_EXTERNAL_INTERFACE = "wan";
    public static final String DEFAULT_PUBLIC_URL = "https://127.0.0.1:8443";
    public static final String DEFAULT_KURJUN_REPO =
            "http://repo.critical-factor.com:8080/rest/kurjun/templates/public";

    private static PropertiesConfiguration PROPERTIES = loadProperties();


    public static PropertiesConfiguration loadProperties()
    {
        PropertiesConfiguration config = null;
        try
        {
            config = new PropertiesConfiguration( String.format( "%s/subutaisystem.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading subutaisettings.cfg file.", e );
        }
        return config;
    }

    // Kurjun Settings


    public static String[] getGlobalKurjunUrls() throws ConfigurationException
    {

        String[] globalKurjunUrls = PROPERTIES.getStringArray( "globalKurjunUrls" );
        if ( globalKurjunUrls.length < 1 )
        {
            globalKurjunUrls = new String[] { DEFAULT_KURJUN_REPO };
        }
        validateGlobalKurjunUrls( globalKurjunUrls );

        return globalKurjunUrls;

        //        String urls = String.valueOf( PROPERTIES.getProperty( "globalKurjunUrls" ) );
        //        String replace = urls.replace( "[", "" );
        //        String replace1 = replace.replace( "]", "" );
        //
        //        return new ArrayList<String>( Arrays.asList( replace1.split( "," ) ) );
    }


    public static void setGlobalKurjunUrls( String[] urls ) throws ConfigurationException
    {
        validateGlobalKurjunUrls( urls );
        saveProperty( "globalKurjunUrls", urls );
    }


    protected static void validateGlobalKurjunUrls( final String[] urls ) throws ConfigurationException
    {
        for ( String url : urls )
        {
            try
            {
                new URL( url );
            }
            catch ( MalformedURLException e )
            {
                throw new ConfigurationException( "Invalid URL: " + url );
            }
        }
    }

    // Network Settings


    public static String getExternalIpInterface()
    {
        return PROPERTIES.getString( "externalInterfaceName", DEFAULT_EXTERNAL_INTERFACE );
    }


    public static void setExternalIpInterface( String externalInterfaceName )
    {
        saveProperty( "externalInterfaceName", externalInterfaceName );
    }


    public static int getOpenPort()
    {
        return PROPERTIES.getInt( "openPort", ChannelSettings.OPEN_PORT );
    }


    public static int getSecurePortX1()
    {
        return PROPERTIES.getInt( "securePortX1", ChannelSettings.SECURE_PORT_X1 );
    }


    public static int getSecurePortX2()
    {
        return PROPERTIES.getInt( "securePortX2", ChannelSettings.SECURE_PORT_X2 );
    }


    public static int getSecurePortX3()
    {
        return PROPERTIES.getInt( "securePortX3", ChannelSettings.SECURE_PORT_X3 );
    }


    public static int getSpecialPortX1()
    {
        return PROPERTIES.getInt( "specialPortX1", ChannelSettings.SPECIAL_PORT_X1 );
    }


    public static void setOpenPort( int openPort )
    {
        saveProperty( "openPort", openPort );
    }


    public static void setSecurePortX1( int securePortX1 )
    {
        saveProperty( "securePortX1", securePortX1 );
    }


    public static void setSecurePortX2( int securePortX2 )
    {
        saveProperty( "securePortX2", securePortX2 );
    }


    public static void setSecurePortX3( int securePortX3 )
    {
        saveProperty( "securePortX3", securePortX3 );
    }


    public static void setSpecialPortX1( int specialPortX1 )
    {
        saveProperty( "specialPortX1", specialPortX1 );
    }


    // Security Settings


    public static boolean getEncryptionState()
    {
        return PROPERTIES.getBoolean( "encryptionEnabled", false );
    }


    public static boolean getRestEncryptionState()
    {
        return PROPERTIES.getBoolean( "restEncryptionEnabled", false );
    }


    public static boolean getIntegrationState()
    {
        return PROPERTIES.getBoolean( "integrationEnabled", false );
    }


    public static boolean getKeyTrustCheckState()
    {
        return PROPERTIES.getBoolean( "keyTrustCheckEnabled", false );
    }


    public static void setEncryptionState( boolean encryptionEnabled )
    {
        saveProperty( "encryptionEnabled", encryptionEnabled );
    }


    public static void setRestEncryptionState( boolean restEncryptionEnabled )
    {
        saveProperty( "restEncryptionEnabled", restEncryptionEnabled );
    }


    public static void setIntegrationState( boolean integrationEnabled )
    {
        saveProperty( "integrationEnabled", integrationEnabled );
    }


    public static void setKeyTrustCheckState( boolean keyTrustCheckEnabled )
    {
        saveProperty( "keyTrustCheckEnabled", keyTrustCheckEnabled );
    }


    // Peer Settings


    public static boolean isRegisteredToHub()
    {
        return PROPERTIES.getBoolean( "isRegisteredToHub", false );
    }


    public static void setRegisterToHubState( boolean registrationState )
    {
        saveProperty( "isRegisteredToHub", registrationState );
    }


    public static String getPublicUrl()
    {
        return PROPERTIES.getString( "publicURL", DEFAULT_PUBLIC_URL );
    }


    public static void setPublicUrl( String publicUrl )
    {
        saveProperty( "publicUrl", publicUrl );
    }


    protected static void saveProperty( final String name, final Object value )
    {
        try
        {
            PROPERTIES.setProperty( name, value );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving subutaisettings.cfg file.", e );
        }
    }
}
