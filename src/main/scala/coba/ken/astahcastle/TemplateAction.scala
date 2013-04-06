package coba.ken.astahcastle

import scala.xml.XML
import scala.xml.Elem

import java.io.File

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

class TemplateAction extends IPluginActionDelegate {
  @throws(classOf[IPluginActionDelegate.UnExpectedException])
  override def run(window:IWindow):java.lang.Object = {
    try {
      val api = AstahAPI.getAstahAPI
      val prjAccessor = api.getProjectAccessor
      prjAccessor.getProject

      val filechooser = new JFileChooser
    
      val file = filechooser.showOpenDialog(window.getParent) match {
        case JFileChooser.APPROVE_OPTION => filechooser.getSelectedFile
        case JFileChooser.CANCEL_OPTION => null
        case JFileChooser.ERROR_OPTION => throw new IPluginActionDelegate.UnExpectedException
      }

      importComment(file,prjAccessor)
    } catch {
      case e:ProjectNotFoundException => {
        val message = "Project is not opened.Please open the project or create new project."
        JOptionPane.showMessageDialog(window.getParent,
                                      message,
                                      "Warning",
                                      JOptionPane.WARNING_MESSAGE)
      }
      case e:Exception => {
        JOptionPane.showMessageDialog(window.getParent,
                                      "Unexpected error has occurred.",
                                      "Alert",
                                      JOptionPane.ERROR_MESSAGE)
        throw new IPluginActionDelegate.UnExpectedException
      }
    }
    null
  }

  def importComment(xmlFile:File, prjAcc:ProjectAccessor) = {
    val xml = XML.loadFile(xmlFile)

    val members = xml \ "members" \ "member"
    members foreach { member =>
      val name = (member \ "@name").toString
      val definition = member.child.mkString.split("\n").filter(!_.isEmpty).map(_.trim).mkString("\n")

      setDefinition(prjAcc, name, definition)
    }
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
  def setDefinition(prjAcc:ProjectAccessor, name:String, definition:String):Unit = {

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

    // if target is operation, we need additional matching by parameter names.
    def parameterList(elem:INamedElement):String = {
      def getQualifiedTypeExpression(p:IParameter): String = {
        p.getQualifiedTypeExpression match {
          case "int" => "System.Int32"
          case s     => s.split("::").mkString(".")
        }
      }

      elem match {
        case op:IOperation => {
          val params = op.getParameters
          val fullQualifiedParams = params.map(getQualifiedTypeExpression(_))
          if (!fullQualifiedParams.isEmpty) fullQualifiedParams.reduce(_ ++ "," ++ _) else ""
        }
      }
    }

    val matched = {
      foundElements.filter( elem =>
        elem match {
          case e: IOperation => {
            val parameterString = parameterList(e) match {
              case "" => ""
              case s  => "(" + s + ")"
            }
            val foundFullNameWithParams = e.getFullName(".") + parameterString
            if (foundFullNameWithParams == fullNameWithParams) true else false
          }
          case _ => true
        })
    }

    matched.map(_.setDefinition(definition))
  }
}
