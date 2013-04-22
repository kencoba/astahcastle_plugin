package coba.ken.astahcastle

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith( classOf[JUnitRunner] )
class XMLDocumentImportUtilTest extends FunSuite {
  import coba.ken.astahcastle.XMLDocumentImportUtil

  test("convertTypeExpression:") {
    val pre0 = "sbyte"
    val post0 = "System.SByte"
    assert(XMLDocumentImportUtil.convertTypeExpression(pre0) === post0)

    val pre1 = "string[]"
    val post1 = "System.String[]"
    assert(XMLDocumentImportUtil.convertTypeExpression(pre1) === post1)

    val pre2 = "Mame::Original::Class"
    val post2 = "Mame.Original.Class"
    assert(XMLDocumentImportUtil.convertTypeExpression(pre2) === post2)
  }
}
