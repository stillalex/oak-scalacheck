package org.apache.jackrabbit.api.security.authorization

import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager
import org.apache.jackrabbit.api.security.TestUtils.assertProp
import org.apache.jackrabbit.api.security.TestUtils.authIdGen
import org.apache.jackrabbit.api.security.TestUtils.genUnique
import org.apache.jackrabbit.oak.AbstractSecurityTest
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop.propBoolean

class PrivilegeManagerTest extends AbstractSecurityTest {

  // TODO test cycles

  var pm: PrivilegeManager = null
  var am: JackrabbitAccessControlManager = null

  @org.junit.Before
  override def before() {
    super.before();
    pm = getPrivilegeManager(root)
    am = getAccessControlManager(root);
  }

  @org.junit.Test
  def priv1() {
    val test = Prop.forAll(genUnique(authIdGen)) { (s: String) =>
      val in = pm.registerPrivilege(s, false, Array())
      val out = pm.getPrivilege(s)
      in.getName.equals(out.getName) && !out.isAbstract() && pm.getRegisteredPrivileges.map(_.getName).contains(s)
    }
    assertProp(test)
  }

  @org.junit.Ignore("wip")
  @org.junit.Test
  def priv2() {
    val test = Prop.forAll(Gen.listOfN(5, genUnique(authIdGen))) { (opts: List[String]) =>
      val s = opts.head
      val ls = opts.tail

      ls.foreach(p => pm.registerPrivilege(p, false, Array()))

      val in = pm.registerPrivilege(s, false, ls.toArray)
      val out = pm.getPrivilege(s)
      in.getName.equals(out.getName) && !out.isAbstract()
    }
    assertProp(test)
  }

}
