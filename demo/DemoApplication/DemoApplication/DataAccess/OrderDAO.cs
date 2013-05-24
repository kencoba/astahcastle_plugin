using System.Collections.Generic;

namespace Application.DataAccess
{
	/**
	 * ///<summary>
	 * ///注文DAO
	 * ///</summary>
	 */
	public interface OrderDAO
	{
		/**
		 * ///<summary>
		 * ///注文検索
		 * ///</summary>
		 */
		IList<string[]> findOrder();

	}

}

