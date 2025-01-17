package org.xutils.common.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.util.concurrent.Executor;

/**
 * 异步任务的代理类(仅在task包内可用)
 *
 * @param <ResultType>
 */
/* package */class TaskProxy<ResultType> extends AbsTask<ResultType> {

	/* package */static final InternalHandler sHandler = new InternalHandler();
	/* package */static final PriorityExecutor sDefaultExecutor = new PriorityExecutor();

	private final AbsTask<ResultType> task;
	private final Executor executor;

	/* package */TaskProxy(AbsTask<ResultType> task) {
		super(task);
		this.task = task;
		this.task.setTaskProxy(this);
		this.setTaskProxy(null);
		Executor taskExecutor = task.getExecutor();
		if (taskExecutor == null) {
			taskExecutor = sDefaultExecutor;
		}
		this.executor = taskExecutor;
	}

	@Override
	protected final ResultType doBackground() throws Throwable {
		this.setStateInner(State.WAITING);
		this.onWaiting();
		PriorityRunnable runnable = new PriorityRunnable(task.getPriority(),
				new Runnable() {
					@Override
					public void run() {
						try {
							// 等待过程中取消
							if (TaskProxy.this.isCancelled()) {
								throw new Callback.CancelledException("");
							}

							// start running
							TaskProxy.this.setStateInner(State.STARTED);
							TaskProxy.this.onStarted();

							if (TaskProxy.this.isCancelled()) { // 开始时取消
								throw new Callback.CancelledException("");
							}

							// 执行task, 得到结果.
							task.setResult(task.doBackground());
							TaskProxy.this.setResult(task.getResult());

							// 未在doBackground过程中取消成功
							if (TaskProxy.this.isCancelled()) {
								throw new Callback.CancelledException("");
							}

							// 执行成功
							TaskProxy.this.setStateInner(State.SUCCESS);
							TaskProxy.this.onSuccess(task.getResult());
						} catch (Callback.CancelledException cex) {
							TaskProxy.this.setStateInner(State.CANCELLED);
							TaskProxy.this.onCancelled(cex);
						} catch (Throwable ex) {
							TaskProxy.this.setStateInner(State.ERROR);
							TaskProxy.this.onError(ex, false);
						}

						// finished
						TaskProxy.this.onFinished();
					}
				});
		this.executor.execute(runnable);
		return null;
	}

	@Override
	protected void onWaiting() {
		sHandler.obtainMessage(MSG_WHAT_ON_WAITING, this).sendToTarget();
	}

	@Override
	protected void onStarted() {
		sHandler.obtainMessage(MSG_WHAT_ON_START, this).sendToTarget();
	}

	@Override
	protected void onSuccess(ResultType result) {
		sHandler.obtainMessage(MSG_WHAT_ON_SUCCESS, this).sendToTarget();
	}

	@Override
	protected void onError(Throwable ex, boolean isCallbackError) {
		sHandler.obtainMessage(MSG_WHAT_ON_ERROR, new ArgsObj(this, ex))
				.sendToTarget();
	}

	@Override
	protected void onUpdate(int flag, Object... args) {
		// obtainMessage(int what, int arg1, int arg2, Object obj), arg2 not be
		// used.
		sHandler.obtainMessage(MSG_WHAT_ON_UPDATE, flag, flag,
				new ArgsObj(this, args)).sendToTarget();
	}

	@Override
	protected void onCancelled(Callback.CancelledException cex) {
		sHandler.obtainMessage(MSG_WHAT_ON_CANCEL, new ArgsObj(this, cex))
				.sendToTarget();
	}

	@Override
	protected void onFinished() {
		sHandler.obtainMessage(MSG_WHAT_ON_FINISHED, this).sendToTarget();
	}

	private void setStateInner(State state) {
		this.setState(state);
		this.task.setState(state);
	}

	@Override
	public final Priority getPriority() {
		return task.getPriority();
	}

	@Override
	public final Executor getExecutor() {
		return this.executor;
	}

	// ########################### inner type #############################
	private static class ArgsObj {
		final TaskProxy taskProxy;
		final Object[] args;

		public ArgsObj(TaskProxy taskProxy, Object... args) {
			this.taskProxy = taskProxy;
			this.args = args;
		}
	}

	private final static int MSG_WHAT_BASE = 1000000000;
	private final static int MSG_WHAT_ON_WAITING = MSG_WHAT_BASE + 1;
	private final static int MSG_WHAT_ON_START = MSG_WHAT_BASE + 2;
	private final static int MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 3;
	private final static int MSG_WHAT_ON_ERROR = MSG_WHAT_BASE + 4;
	private final static int MSG_WHAT_ON_UPDATE = MSG_WHAT_BASE + 5;
	private final static int MSG_WHAT_ON_CANCEL = MSG_WHAT_BASE + 6;
	private final static int MSG_WHAT_ON_FINISHED = MSG_WHAT_BASE + 7;

	/* package */final static class InternalHandler extends Handler {

		private InternalHandler() {
			super(Looper.getMainLooper());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj == null) {
				throw new IllegalArgumentException("msg must not be null");
			}
			TaskProxy taskProxy = null;
			Object[] args = null;
			if (msg.obj instanceof TaskProxy) {
				taskProxy = (TaskProxy) msg.obj;
			} else if (msg.obj instanceof ArgsObj) {
				ArgsObj argsObj = (ArgsObj) msg.obj;
				taskProxy = argsObj.taskProxy;
				args = argsObj.args;
			}
			if (taskProxy == null) {
				throw new RuntimeException("msg.obj not instanceof TaskProxy");
			}

			try {
				switch (msg.what) {
				case MSG_WHAT_ON_WAITING: {
					taskProxy.task.onWaiting();
					break;
				}
				case MSG_WHAT_ON_START: {
					taskProxy.task.onStarted();
					break;
				}
				case MSG_WHAT_ON_SUCCESS: {
					taskProxy.task.onSuccess(taskProxy.getResult());
					break;
				}
				case MSG_WHAT_ON_ERROR: {
					assert args != null;
					Throwable throwable = (Throwable) args[0];
					LogUtil.e(throwable.getMessage(), throwable);
					taskProxy.task.onError((Throwable) args[0], false);
					break;
				}
				case MSG_WHAT_ON_UPDATE: {
					taskProxy.task.onUpdate(msg.arg1, args);
					break;
				}
				case MSG_WHAT_ON_CANCEL: {
					assert args != null;
					taskProxy.task
							.onCancelled((org.xutils.common.Callback.CancelledException) args[0]);
					break;
				}
				case MSG_WHAT_ON_FINISHED: {
					taskProxy.task.onFinished();
					break;
				}
				default: {
					break;
				}
				}
			} catch (Throwable ex) {
				taskProxy.setStateInner(State.ERROR);
				if (msg.what != MSG_WHAT_ON_ERROR) {
					taskProxy.task.onError(ex, true);
				} else if (x.isDebug()) {
					throw new RuntimeException(ex);
				}
			}
		}
	}
}
