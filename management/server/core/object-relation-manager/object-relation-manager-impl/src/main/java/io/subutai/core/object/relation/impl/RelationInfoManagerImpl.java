package io.subutai.core.object.relation.impl;


import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationInfoManager;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.object.relation.impl.dao.RelationDataService;
import io.subutai.core.object.relation.impl.model.RelationChallengeImpl;
import io.subutai.core.object.relation.impl.model.RelationInfoImpl;
import io.subutai.core.object.relation.impl.model.RelationLinkImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


public class RelationInfoManagerImpl implements RelationInfoManager
{
    private static final Logger logger = LoggerFactory.getLogger( RelationInfoManagerImpl.class );
    private RelationDataService relationDataService;
    private IdentityManager identityManager;
    private SecurityManager securityManager;


    public RelationInfoManagerImpl( final RelationDataService relationDataService,
                                    final IdentityManager identityManager, final SecurityManager securityManager )
    {
        this.identityManager = identityManager;
        this.relationDataService = relationDataService;
        this.securityManager = securityManager;
    }


    public boolean decryptAndVerifyChallenge( final String signedMessage, final String challengeKeyId )
            throws RelationVerificationException
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            PGPSecretKeyRing secretKeyRing = keyManager.getSecretKeyRing( null );

            byte[] extractedText = encryptionTool.extractClearSignContent( signedMessage.getBytes() );
            byte[] decrypted = encryptionTool.decrypt( extractedText, secretKeyRing, "" );

            String decryptedMessage = new String( decrypted, "UTF-8" );
            RelationChallengeImpl relationChallengeImpl =
                    JsonUtil.fromJson( decryptedMessage, RelationChallengeImpl.class );

            if ( relationChallengeImpl.getTtl() > 0
                    && relationChallengeImpl.getTimestamp() + relationChallengeImpl.getTtl() < System
                    .currentTimeMillis() )
            {
                throw new RelationVerificationException( "Relation token timeout exceeded." );
            }

            if ( relationChallengeImpl.getStatus() == RelationStatus.STATED )
            {
                RelationChallengeImpl persistedToken =
                        relationDataService.getRelationToken( relationChallengeImpl.getToken() );
                if ( relationChallengeImpl.equals( persistedToken ) )
                {
                    persistedToken.setStatus( RelationStatus.VERIFIED );
                    relationDataService.update( persistedToken );
                }
                else
                {
                    throw new RelationVerificationException( "Relation tokens didn't match." );
                }
            }
            else
            {
                throw new RelationVerificationException( "Abnormal relation token status." );
            }

            PGPPublicKeyRing publicKey = keyManager.getPublicKeyRing( challengeKeyId );
            if ( publicKey == null || !encryptionTool.verifyClearSign( signedMessage.getBytes(), publicKey ) )
            {
                throw new RelationVerificationException( "Relation message verification failed." );
            }

            return true;
        }
        catch ( PGPException ex )
        {
            throw new RelationVerificationException(
                    "Relation verification failed (possible cause of error: clear sign document extraction failed, "
                            + "failed to decrypt message or failed to verify message).", ex );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RelationVerificationException( e );
        }
    }


    private RelationLink getDelegatedUserLink( RelationLink target ) throws RelationVerificationException
    {
        User activeUser = identityManager.getActiveUser();
        UserDelegate delegatedUser = null;
        if ( activeUser != null )
        {
            delegatedUser = identityManager.getUserDelegate( activeUser.getId() );
        }
        else
        {
            // if activeUser == null then select first RelationLink with Ownership level User
            // and verify that it is User type

            // to get RelationLink type of User with Ownership level User
            // walk through relation tree and get desired Object

            // relationLink = trustedObject
            // get list of all relations where trustedObject = relationLink and RelationInfo.ownership = 'User'
            // select first object where relation.source.context = PermissionObject.IdentityManagement.getName()

            // Ownership User already defines that trusted object is owned by source or target
            List<Relation> relationList = relationDataService.getTrustedRelationsByOwnership( target, Ownership.USER );
            for ( final Relation relation : relationList )
            {
                if ( PermissionObject.IdentityManagement.getName().equals( relation.getSource().getContext() ) )
                {
                    delegatedUser = identityManager.getUserDelegate( relation.getSource().getUniqueIdentifier() );
                    if ( delegatedUser != null )
                    {
                        break;
                    }
                }
                if ( PermissionObject.IdentityManagement.getName().equals( relation.getTarget().getContext() ) )
                {
                    delegatedUser = identityManager.getUserDelegate( relation.getTarget().getUniqueIdentifier() );
                    if ( delegatedUser != null )
                    {
                        break;
                    }
                }
            }
        }

        if ( delegatedUser == null )
        {
            throw new RelationVerificationException( "Failed to get trusted object owner" );
        }
        return delegatedUser;
    }


    private RelationMeta getUserLinkRelation( RelationLink target ) throws RelationVerificationException
    {
        return new RelationMeta( getDelegatedUserLink( target ), target, target.getLinkId() );
    }


    // TODO also verify source of trust, like if A -> B -> C, check does really A has permission to create relation
    // between B and C
    // C should give correct verification through relationship path.
    private boolean isRelationValid( final RelationInfo relationInfo, final RelationMeta relationMeta )
    {
        if ( !SystemSettings.getKeyTrustCheckState() )
        {
            return true;
        }

        Set<RelationLink> relationLinks = Sets.newHashSet();

        RelationLinkImpl target = new RelationLinkImpl( relationMeta.getSource() );
        List<Relation> byTargetRelations = relationDataService.findByTarget( target );

        RelationLinkImpl object = new RelationLinkImpl( relationMeta.getObject() );
        List<Relation> bySourceRelations = relationDataService.findBySource( target );

        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : byTargetRelations )
        {
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                return compareRelationships( targetRelation.getRelationInfo(), relationInfo ) >= 0;
            }
            int result = getDeeper( relationInfo, targetRelation.getTrustedObject(), object, relationLinks );
            if ( result != -3 )
            {
                return result >= 0;
            }
        }

        // TODO instead of getting deep one/two steps later implement full relation lookup with source - target -
        // object relationship verification
        // relationship verification should be done at transaction point between relation links, checks should be
        // applied towards granting link, as does this granting link has permissions to set new relation link and new
        // relation link doesn't exceed relation link grantee has
        for ( final Relation sourceRelation : bySourceRelations )
        {
            if ( sourceRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                return compareRelationships( sourceRelation.getRelationInfo(), relationInfo ) >= 0;
            }
        }

        return false;
    }


    // return -3 means no relation exist
    private int getDeeper( final RelationInfo relationInfo, final RelationLink target, final RelationLink object,
                           Set<RelationLink> relationLinks )
    {
        if ( !SystemSettings.getKeyTrustCheckState() )
        {
            return 0;
        }
        List<Relation> byTargetRelations = relationDataService.findByTarget( target );
        relationLinks.add( target );
        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : byTargetRelations )
        {
            int compare = compareRelationships( targetRelation.getRelationInfo(), relationInfo );
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                return compare;
            }
            if ( compare >= 0 && !relationLinks.contains( targetRelation.getTrustedObject() ) )
            {
                int result = getDeeper( relationInfo, ( RelationLinkImpl ) targetRelation.getTrustedObject(), object,
                        relationLinks );
                if ( result != -3 )
                {
                    return result;
                }
            }
        }

        return -3;
    }


    /**
     * Compare relationship depending on each relationship property, if relation ownership level differs then this
     * relation is not comparable, other properties simply should match, and data should come in key=value format 1 - a
     * is greater 0 - equal -1 - a is less -2 - incomparable TODO extract this method into RelationInfo.equals (PS.
     * dynamic comparison will be possible)
     */
    private int compareRelationships( RelationInfo a, RelationInfo b )
    {
        int ownership = 0;
        if ( a.getOwnershipLevel() > b.getOwnershipLevel() )
        {
            ownership = 1;
        }
        else if ( a.getOwnershipLevel() < b.getOwnershipLevel() )
        {
            ownership = -1;
        }

        //Calculate permission operations level
        int operation = 0;
        if ( !a.isDeletePermission() && b.isDeletePermission() || !a.isReadPermission() && b.isReadPermission()
                || !a.isUpdatePermission() && b.isUpdatePermission() || !a.isWritePermission() && b
                .isWritePermission() )
        {
            operation = -1;
        }
        else
        {
            operation = 0;
        }

        // This relation comparison logic can be taken out by interface means so that client could implement his own
        // comparison mechanism
        if ( a.getRelationTraits() != null && b.getRelationTraits() != null )
        {
            for ( final Map.Entry<String, String> entry : b.getRelationTraits().entrySet() )
            {
                String keyB = entry.getKey();

                String valueB = entry.getValue();
                String valueA = a.getRelationTraits().get( keyB );
                if ( !Strings.isNullOrEmpty( valueB ) && !valueB.equals( valueA ) )
                {
                    return -1;
                }
            }
        }

        if ( operation == 0 )
        {
            return ownership;
        }
        return operation;
    }


    @Override
    public boolean groupHasWritePermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, true, false, false, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    @Override
    public boolean allHasReadPermissions( final RelationMeta relationMeta )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( true, false, false, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        return isRelationValid( relationInfo, relationMeta );
    }


    public boolean groupHasDeletePermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, false, true, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public boolean groupHasUpdatePermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta =
                new RelationInfoMeta( false, false, true, false, Ownership.GROUP.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public boolean allHasReadPermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( true, false, false, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public boolean allHasWritePermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, true, false, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public boolean allHasDeletePermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, false, false, true, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public boolean allHasUpdatePermissions( final RelationLink relationLink )
    {
        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( false, false, true, false, Ownership.ALL.getLevel() );
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );
        try
        {
            return isRelationValid( relationInfo, getUserLinkRelation( relationLink ) );
        }
        catch ( RelationVerificationException e )
        {
            return false;
        }
    }


    @Override
    public void checkRelation( final RelationLink targetObject, final RelationInfoMeta relationInfoMeta,
                               final String encodedToken ) throws RelationVerificationException
    {
        checkRelation( getDelegatedUserLink( targetObject ), targetObject, relationInfoMeta, encodedToken );
    }


    @Override
    public void checkRelation( final RelationLink source, final RelationLink targetObject,
                               final RelationInfoMeta relationInfoMeta, final String encodedToken )
            throws RelationVerificationException
    {
        if ( !SystemSettings.getKeyTrustCheckState() )
        {
            return;
        }

        if ( !Strings.isNullOrEmpty( encodedToken ) )
        {
            decryptAndVerifyChallenge( encodedToken, source.getKeyId() );
        }

        Set<RelationLink> relationLinks = Sets.newHashSet();
        RelationInfo relationInfo = new RelationInfoImpl( relationInfoMeta );

        RelationLinkImpl target = new RelationLinkImpl( source );
        List<Relation> byTargetRelations = relationDataService.findByTarget( target );

        RelationLinkImpl object = new RelationLinkImpl( targetObject );
        List<Relation> bySourceRelations = relationDataService.findBySource( target );

        // When relation info is found check that relation was granted from verified source
        for ( final Relation targetRelation : byTargetRelations )
        {
            if ( targetRelation.getRelationStatus() == RelationStatus.STATED && Strings.isNullOrEmpty( encodedToken ) )
            {
                throw new RelationVerificationException( "You should pass relation token challenge first." );
            }
            if ( targetRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted
                if ( compareRelationships( targetRelation.getRelationInfo(), relationInfo ) >= 0 )
                {
                    return;
                }
                else
                {
                    throw new RelationVerificationException( "Your relation has insufficient permissions." );
                }
            }
            int result = getDeeper( relationInfo, targetRelation.getTrustedObject(), object, relationLinks );
            if ( result != -3 )
            {
                if ( !( result >= 0 ) )
                {
                    throw new RelationVerificationException( "Your relation has insufficient permissions." );
                }
            }
        }

        // TODO instead of getting deep one/two steps later implement full relation lookup with source - target -
        // object relationship verification
        // relationship verification should be done at transitional point between relation links, checks should be
        // applied towards granting link, such as: does this granting link has permissions to set new relation link
        // and new relation link doesn't exceed relation link grantee has
        for ( final Relation sourceRelation : bySourceRelations )
        {
            if ( sourceRelation.getRelationStatus() == RelationStatus.STATED && Strings.isNullOrEmpty( encodedToken ) )
            {
                throw new RelationVerificationException( "You should pass relation token challenge first." );
            }
            if ( sourceRelation.getTrustedObject().equals( object ) )
            {
                // Requested relation should be less then or equal to relation that was granted

                if ( compareRelationships( sourceRelation.getRelationInfo(), relationInfo ) >= 0 )
                {
                    return;
                }
                else
                {
                    throw new RelationVerificationException( "Your relation has insufficient permissions." );
                }
            }
        }
    }
}
