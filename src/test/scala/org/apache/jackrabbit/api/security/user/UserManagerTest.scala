package org.apache.jackrabbit.api.security.user

import org.apache.jackrabbit.api.security.TestUtils.assertProp
import org.apache.jackrabbit.api.security.TestUtils.authIdGen
import org.apache.jackrabbit.api.security.TestUtils.pathGen
import org.apache.jackrabbit.oak.AbstractSecurityTest
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters
import org.apache.jackrabbit.oak.spi.security.principal.PrincipalImpl
import org.apache.jackrabbit.oak.spi.security.user.UserConfiguration
import org.apache.jackrabbit.oak.spi.security.user.UserConstants.PARAM_GROUP_PATH
import org.apache.jackrabbit.oak.spi.security.user.UserConstants.PARAM_USER_PATH
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop.propBoolean

class UserManagerTest extends AbstractSecurityTest {

  var userHome = "/home/users"
  var systemHome = userHome + "/system"
  var groupHome = "/home/groups"
  var um: UserManager = null

  @org.junit.Before
  override def before() {
    val pg: Gen[String] = pathGen("")
    userHome = pg.sample.get
    systemHome = userHome + "/system"
    groupHome = pg.sample.get

    super.before();
    um = getUserManager(root)
  }

  override def getSecurityConfigParameters(): ConfigurationParameters = {
    val uc = ConfigurationParameters.of(PARAM_USER_PATH, userHome, PARAM_GROUP_PATH, groupHome)
    return ConfigurationParameters.of(UserConfiguration.NAME, uc)
  }

  @org.junit.Test
  def group1() {
    val test = Prop.forAll(authIdGen) { (id: String) =>
      val i = um.createGroup(id)
      verifyAuthorizable(i, id, groupHome, "")
    }
    assertProp(test)
  }

  @org.junit.Test
  def group2() {
    val test = Prop.forAll(authIdGen, pathGen(groupHome)) { (id: String, path: String) =>
      val i = um.createGroup(new PrincipalImpl(id), path)
      verifyAuthorizable(i, id, groupHome, path)
    }
    assertProp(test)
  }

  @org.junit.Test
  def system() {
    val test = Prop.forAll(authIdGen, pathGen(systemHome)) {
      (id: String, path: String) =>
        val i = um.createSystemUser(id, path)
        verifyAuthorizable(i, id, systemHome, path)
    }
    assertProp(test)
  }

  @org.junit.Test
  def user1() {
    val test = Prop.forAll(authIdGen, pathGen(userHome)) {
      (id: String, path: String) =>
        val i = um.createUser(id, id, new PrincipalImpl(id), path)
        verifyAuthorizable(i, id, userHome, path)
    }
    assertProp(test)
  }

  def verifyAuthorizable(i: Authorizable, id: String, home: String, intermediatePath: String): Boolean = {
    try {
      i.getID().equals(id) &&
        i.getPath.startsWith(home) &&
        i.getPath.contains(intermediatePath) &&
        um.getAuthorizable(id).getID.equals(id) &&
        um.getAuthorizableByPath(i.getPath).getID.equals(id)
    } finally {
      i.remove()
    }
  }
}