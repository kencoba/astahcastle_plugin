using System.Collections.Generic;

namespace Application.DataAccess
{
    /// <summary>
    /// ���̃��f���ɂ̓R�����g���Ȃ������B
    /// ���̃R�����g�����f����邱�Ƃ��m�F����B
    /// </summary>
	public interface OrderDAO
	{
		/**
		 * ///<summary>
		 * ///��������
         * /// ���\�b�h��`���A�ȉ��̌`�ɂȂ��Ă����B
         * ///IList_string[]_ findOrder()
         * /// ���̃R�����g�����f����邱�Ƃ��m�F����B
		 * ///</summary>
		 * 
		 */
		IList<string[]> findOrder();

	}

}

