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

  def getMembers(xml:Elem):NodeSeq = xml \ "members" \ "member"

  def getName(member:Node):String = (member \ "@name").toString

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

      def logUnmatched(name:String,definition:String): Unit = {
        log.println("<result>" +
                    "<xml-name><![CDATA[" + name + "]]></xml-name>" + 
                    "<model-name></model-name>" +
                    "<updated value=\"false\"/>" +
                    "<original-definition></original-definition>" +
                    "<new-definition><![CDATA[" + definition + "]]></new-definition>" +
                    "</result>")
      }

      def logMatched(name:String,res:List[(String,String,String)]):Unit = {
          res.foreach(r =>
            log.println("<result>" +
                        "<xml-name><![CDATA[" + name + "]]></xml-name>" +
                        "<model-name><![CDATA[" + r._1 + "]]></model-name>" +
                        "<updated value=\"true\"/>" +
                        "<original-definition><![CDATA[" + r._2 + "]]></original-definition>" +
                        "<new-definition><![CDATA[" + r._3 + "]]></new-definition>" +
                        "</result>"))
      }

      result match {
        case result if (result.isEmpty) => logUnmatched(name,definition)
        case _ => logMatched(name,result)
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
            val foundFullNameWithParams = getFullName(elem)
            logger.info("\n[" + foundFullNameWithParams + "]<=model\n[" + fullNameWithParams + "]<=xml")
            foundFullNameWithParams == fullNameWithParams
          }
          case _ => true
        })
    }

    (for (e <- matched) yield {
      val original = e.getDefinition
      e.setDefinition(definition)
      (getFullName(e),original,definition)
    }).toList
  }

  def getFullName(elem:INamedElement):String = {
    elem match {
      case e: IOperation => getOperationString(e) //{e.getFullName(".") + getParameterString(e)}
      case _ => elem.getFullName(".")
    }
  }

  def getOperationString(e:IOperation):String = {
    logger.info(getParametersString(e.getParameters))
    getParametersString(e.getParameters) match {
      case "" => e.getFullName(".")
      case s  => e.getFullName(".") + "(" + s + ")"
    }
  }

  def getParametersString(params:Array[IParameter]):String = {
    val qualifiedParams:Array[String] = params.map(qualifiedParameterExp(_))
    if (!qualifiedParams.isEmpty) qualifiedParams.reduce(_ ++ "," ++ _) else ""
  }

  def qualifiedParameterExp(p:IParameter): String = {
    if (hasGeneric(typeExp(p))) {
      typeExpWithGeneric(p)
    } else {
      typeExpWithNoGeneric(p)
    }
  }

  def hasGeneric(s:String) = s.contains("<")

  def typeExp(p:IParameter):String = {
    val namespace = p.getType.getFullNamespace(".")
    namespace match {
      case "" => p.getTypeExpression // possibly simple type
      case _ => namespace + "." + p.getTypeExpression
    }
  }

  def genericTypeName(p:IParameter):String = typeExp(p).split("<")(0)

  def typeExpWithGeneric(p:IParameter):String = {
    import scala.collection.JavaConverters._
    val templateBindings = p.getType.getTemplateBindings
    val actualMap = templateBindings(0).getQualifiedActualMap.asScala
    val typeParams = (
      for ((key,value) <- actualMap)
      yield {
        logger.info(value.toString.replace("::","."))
        value.toString.replace("::",".")
      }
    ).mkString(",")

    aliasToFullName(genericTypeName(p)) + "{" + typeParams + "}"
  }

  def typeExpWithNoGeneric(p:IParameter):String = aliasToFullName(genericTypeName(p))

  def aliasToFullName(exp:String): String = {
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

}

