/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.integration.idm.config;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.integration.AbstractJPADeploymentTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.ArchiveUtils.addDependency;
import static org.picketlink.test.integration.ArchiveUtils.getCurrentProjectVersion;

/**
 * @author Pedro Igor
 *
 */
public class JPAAutoConfigurationConfigurationTestCase extends AbstractJPADeploymentTestCase {
    
    @Inject
    private JPAStoreConfigurationObserver configurationObserver;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = createDeployment(JPAAutoConfigurationConfigurationTestCase.class, JPAStoreConfigurationObserver.class);
        
        addDependency(archive, "org.picketlink:picketlink-idm-simple-schema:" + getCurrentProjectVersion());
        
        return archive;
    }
    
    @Test
    public void testConfiguration() throws Exception {
        IdentityConfiguration identityConfiguration = this.configurationObserver.getIdentityConfigurationBuilder().build();
        
        List<? extends IdentityStoreConfiguration> configuredStores = identityConfiguration.getStoreConfiguration();
        
        assertEquals(1, configuredStores.size());
        
        IdentityStoreConfiguration identityStoreConfiguration = configuredStores.get(0);
        
        assertEquals(JPAIdentityStoreConfiguration.class, identityStoreConfiguration.getClass());
    }

    @Test
    public void testBasicFeatures() throws Exception {
        User john = new User("john");

        this.identityManager.add(john);

        Role tester = new Role("Tester");

        this.identityManager.add(tester);

        Group qaGroup = new Group("QA");

        this.identityManager.add(qaGroup);

        this.relationshipManager.grantRole(john, tester);
        this.relationshipManager.addToGroup(john, qaGroup);
        this.relationshipManager.grantGroupRole(john, tester, qaGroup);

        assertTrue(this.relationshipManager.hasRole(john, tester));
        assertTrue(this.relationshipManager.isMember(john, qaGroup));
        assertTrue(this.relationshipManager.hasGroupRole(john, tester, qaGroup));
    }
 
    @ApplicationScoped
    public static class JPAStoreConfigurationObserver {
        
        private IdentityConfigurationBuilder identityConfigurationBuilder;

        public void observeIdentityConfigurationEvent(@Observes IdentityConfigurationEvent event) {
            this.identityConfigurationBuilder = event.getConfig();
        }
        
        public IdentityConfigurationBuilder getIdentityConfigurationBuilder() {
            return this.identityConfigurationBuilder;
        }
    }
    
}