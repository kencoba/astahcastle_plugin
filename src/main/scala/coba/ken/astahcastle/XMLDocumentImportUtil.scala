package coba.ken.astahcastle

import scala.xml.XML
import scala.xml.Elem

import java.io.File
import java.io.PrintWriter

import javax.swing.JOptionPane
import javax.swing.JFileChooser

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException
import com.change_vision.jude.api.inf.project.ProjectAccessor
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate
import com.change_vision.jude.api.inf.ui.IWindow
import com.change_vision.jude.api.inf.editor.TransactionManager
import com.change_vision.jude.api.inf.editor.ModelEditorFactory
import com.change_vision.jude.api.inf.editor.BasicModelEditor
import com.change_vision.jude.api.inf.model._

/**
 *
 * astah* plugin for Import Visual Studio XML Document.
 *
 * Before run this plugin, close property window on astah. 
 *
 * @author Kenichi Kobayashi
 */
object XMLDocumentImportUtil {
  import scala.xml.NodeSeq
  import scala.xml.Node

  import org.slf4j.LoggerFactory
  private val logger = LoggerFactory.getLogger(classOf[TemplateAction])
  private val logFilename = "_astarcastle_log.xml"

  def getMembers(xml:Elem):NodeSeq = {
    xml \ "members" \ "member"
  }

  def getName(member:Node):String = {
    (member \ "@name").toString
  }

  def getDefinition(member:Node) = {
     member.child.mkString.split("\n").filter(!_.isEmpty).map(_.trim).mkString("\n")
  }

  def importComment(xmlFile:File, prjAcc:ProjectAccessor) = {
    val log = new PrintWriter(prjAcc.getProjectPath + logFilename)
    log.println("<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>")
    log.println("<results>")

    val xml = XML.loadFile(xmlFile)

    val members = getMembers(xml)
    members foreach { member =>
      val name = getName(member)
      val definition = getDefinition(member)

      val result = setDefinition(prjAcc, name, definition)

      result match {
        case result if (result.isEmpty) => log.println("<result>" +
                                                       "<xml-name><![CDATA[" + name + "]]></xml-name>" + 
                                                       "<model-name></model-name>" +
                                                       "<updated value=\"false\"/>" +
                                                       "<original-definition></original-definition>" +
                                                       "<new-definition><![CDATA[" + definition + "]]></new-definition>" +
                                                       "</result>")
        case _ => {
          result.foreach(r => log.println("<result>" +
                                          "<xml-name><![CDATA[" + name + "]]></xml-name>" +
                                          "<model-name><![CDATA[" + r._1 + "]]></model-name>" +
                                          "<updated value=\"true\"/>" +
                                          "<original-definition><![CDATA[" + r._2 + "]]></original-definition>" +
                                          "<new-definition><![CDATA[" + r._3 + "]]></new-definition>" +
                                          "</result>"))
        }
      }
    }

    log.println("</results>")
    log.close()
  }

  def getTargetType(typeKey:String) = {
    typeKey match {
      case "T" => classOf[IClass]
      case "M" => classOf[IOperation]
      case "F" => classOf[IAttribute]
      case "P" => classOf[IAttribute]
      case _ => sys.error("Unknown typeKey = " + typeKey)
    }
  }

  def getQualifiedTypeExpression(p:IParameter): String = {
    val exp = p.getQualifiedTypeExpression
    convertTypeExpression(exp)
  }

  def convertTypeExpression(exp:String): String = {
    val (name, arrayType) = exp.span(_ != '[')
    val typeName = name match {
      case "object" => "System.Object"
      case "sbyte"  => "System.SByte"
      case "byte"   => "System.Byte"
      case "char"   => "System.Char"
      case "short"  => "System.Int16"
      case "ushort" => "System.UInt16"
      case "int"    => "System.Int32"
      case "uint"   => "System.UInt32"
      case "long"   => "System.Int64"
      case "ulong"  => "System.UInt64"
      case "float"  => "System.Single"
      case "double" => "System.Double"
      case "string" => "System.String"
      case "bool"   => "System.Boolean"
      case s        => s.split("::").mkString(".")
    }
    typeName ++ arrayType
  }

  // if target is operation, we need additional matching by parameter names.
  // TODO check parameter of array.
  def parameterList(elem:INamedElement):String = {
    elem match {
      case op:IOperation => {
        val params = op.getParameters
        val fullQualifiedParams:Array[String] = params.map(getQualifiedTypeExpression(_))
        if (!fullQualifiedParams.isEmpty) fullQualifiedParams.reduce(_ ++ "," ++ _) else ""
      }
    }
  }

  /**
   * set definition in Astah model.
   *
   * the format of `name` attribute string is as below.
   *
   * name      = C#'s name character of class,interface,package,etc.
   * 
   * nameAttr  = typeKey : fullName [ "(" params ")"]
   * typeKey   = "T" | "M" | "F" | "P"
   * fullName  = name { "." name }
   * params    = fullName { "," fullName }
   *
   * @param prjAcc ProjectAccessor instance.
   * @param name   name attribute in XML file by VS2008 XML generator.
   * @param definition body of member tag in XML file.
   *
   */
  def setDefinition(prjAcc:ProjectAccessor, name:String, definition:String):List[(String,String,String)] = {

    val typeAndFullName = name.split(":")
    val typeKey = typeAndFullName(0)
    val fullNameWithParams = typeAndFullName(1)
    val fullNameAndParams = fullNameWithParams.split("[\\(\\)]")
    val fullName = fullNameAndParams(0)
    val params = if (fullNameAndParams.size > 1) fullNameAndParams(1) else ""
    val targetName = fullName.split('.').last

    val targetType = getTargetType(typeKey)

    // find same name elements.
    val foundElements = prjAcc.findElements(targetType, targetName).filter(_.getFullName(".") == fullName)

    val matched = {
      foundElements.filter( elem =>
        elem match {
          case e: IOperation => {
//            val parameterString = parameterList(e) match {
//              case "" => ""
//              case s  => "(" + s + ")"
//            }
//            val foundFullNameWithParams = e.getFullName(".") + parameterString
            val foundFullNameWithParams = getFullName(elem)
            logger.info("\n[" + foundFullNameWithParams + "]<=model\n[" + fullNameWithParams + "]<=xml")
            foundFullNameWithParams == fullNameWithParams
          }
          case _ => true
        })
    }

    matched.map{ elem => elem.setDefinition(definition)}
    matched.map{ elem => (getFullName(elem),elem.getDefinition,definition)}.toList
  }

  def getFullName(elem:INamedElement):String = {
    elem match {
      case e: IOperation => {
        val parameterString = parameterList(e) match {
          case "" => ""
          case s  => "(" + s + ")"
        }
        e.getFullName(".") + parameterString
      }
      case _ => elem.getFullName(".")
    }
  }
}
