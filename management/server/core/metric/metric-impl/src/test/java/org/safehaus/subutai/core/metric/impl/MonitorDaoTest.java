package org.safehaus.subutai.core.metric.impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.DbUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for MonitorDao
 */
@RunWith( MockitoJUnitRunner.class )
public class MonitorDaoTest
{
    @Mock
    DbUtil dbUtil;
    @Mock
    DataSource dataSource;
    private final static String SUBSCRIBER_ID = "subscriber";
    private final static UUID ENVIRONMENT_ID = UUID.randomUUID();

    MonitorDaoExt monitorDao;


    static class MonitorDaoExt extends MonitorDao
    {

        public MonitorDaoExt( final DataSource dataSource ) throws DaoException
        {
            super( dataSource );
        }


        @Override
        protected void setupDb() throws DaoException
        {
            //no-op
        }


        public void testSetupDB() throws DaoException
        {
            super.setupDb();
        }


        public void setDbUtil( DbUtil dbUtil )
        {
            this.dbUtil = dbUtil;
        }
    }


    private void throwDbException() throws SQLException
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );
        when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );
    }


    @Before
    public void setUp() throws Exception
    {
        monitorDao = new MonitorDaoExt( dataSource );
        monitorDao.setDbUtil( dbUtil );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullDataSource() throws Exception
    {
        new MonitorDao( null );
    }


    @Test
    public void testAddSubscription() throws Exception
    {

        monitorDao.addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        verify( dbUtil ).update( anyString(), anyVararg() );
    }


    @Test( expected = DaoException.class )
    public void testAddSubscriptionException() throws Exception
    {
        throwDbException();

        monitorDao.addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test
    public void testRemoveSubscription() throws Exception
    {

        monitorDao.removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        verify( dbUtil ).update( anyString(), anyVararg() );
    }


    @Test( expected = DaoException.class )
    public void testRemoveSubscriptionException() throws Exception
    {
        throwDbException();

        monitorDao.removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test
    public void testGetEnvironmentSubscribersIds() throws Exception
    {
        ResultSet resultSet = mock( ResultSet.class );
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( resultSet );
        when( resultSet.next() ).thenReturn( true ).thenReturn( false );
        when( resultSet.getString( anyString() ) ).thenReturn( SUBSCRIBER_ID );

        Set<String> subscribersIds = monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID );

        assertTrue( subscribersIds.contains( SUBSCRIBER_ID ) );
    }


    @Test( expected = DaoException.class )
    public void testGetEnvironmentSubscribersIdsException() throws Exception
    {
        throwDbException();

        monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    }


    @Test
    public void testSetupDb() throws Exception
    {
        monitorDao.testSetupDB();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass( String.class );


        verify( dbUtil ).update( sqlCaptor.capture() );

        assertEquals( "create table if not exists monitor_subscriptions(environmentId uuid, subscriberId varchar(100), "
                + " PRIMARY KEY (environmentId, subscriberId));", sqlCaptor.getValue() );
    }


    @Test( expected = DaoException.class )
    public void testSetupDbException() throws Exception
    {

        throwDbException();

        monitorDao.testSetupDB();
    }
}