package br.com.httpmock.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;

import br.com.httpmock.Main.LocalServer;
import br.com.httpmock.utils.Constants;
import br.com.httpmock.utils.HttpUtils;
import br.com.httpmock.utils.StringUtils;
import br.com.httpmock.views.actions.ClearAction;
import br.com.httpmock.views.actions.ClickOnCellAction;
import br.com.httpmock.views.actions.CloseAction;
import br.com.httpmock.views.actions.ExecutionDetailsAction;

public class NetworkView
        extends JFrame
{
    private static final long          serialVersionUID                         = 1L;
    private static final String[]      COLUMN_NAMES                             = new String[] {
            "Em cache",
            "Método",
            "Status",
            "Nome",
            "Tipo",
            "Conteúdo",
            "Request",
            "Response",
            "Seq.",
            "Thread Id",
            ""
    };
    private static final String[]      COLUMNS_LABELS_REFERENCES_FOR_WIDTH      = new String[] {
            " Em cache ",
            " PROPFIND ",
            " Status ",
            " /teste/teste/teste/teste/teste/teste/teste/teste/teste/teste/teste.html ",
            " application/vnd.oasis.opendocument.presentation ",
            null,
            null,
            null,
            " 00000 ",
            null,
            null
    };
    private static final int           CACHED_COLUMN                            = 0;
    private static final int           METHOD_COLUMN                            = 1;
    private static final int           STATUS_COLUMN                            = 2;
    private static final int           URL_PATH_COLUMN                          = 3;
    private static final int           CONTENT_TYPE_COLUMN                      = 4;
    private static final int           CONTENT_COLUMN                           = 5;
    private static final int           REQUEST_ID_COLUMN                        = 6;
    private static final int           RESPONSE_ID_COLUMN                       = 7;
    private static final int           SEQUENCE_COLUMN                          = 8;
    private static final int           THREAD_ID_COLUMN                         = 9;
    private static final String[]      INFO_COLUMN_NAMES                        = new String[] {
            "Modo",
            "Endereço Local",
            "Endereço Proxiado Default",
            "Diretório de Leitura / Gravação",
            ""
    };
    private static final String[]      INFO_COLUMNS_LABELS_REFERENCES_FOR_WIDTH = new String[] {
            " Modo ",
            " Endereço Local ",
            " Endereço Proxiado Default ",
            " Diretório de Leitura / Gravação ",
            null
    };
    private static final int           MODE_COLUMN                              = 0;
    private static final int           LOCAL_SERVER_COLUMN                      = 1;
    private static final int           PROXIED_SERVER_COLUMN                    = 2;
    private static final int           RECORDING_DIRECTORY_COLUMN               = 3;

    private static final Color         FONT_COLOR                               = new Color(32, 32, 32);
    private static final Color         ERROR_FONT_COLOR                         = new Color(224, 32, 32);
    private static final NetworkView   INSTANCE                                 = new NetworkView();

    private DefaultTableModel          dtm;
    private JTextArea                  textArea;
    private JTable                     jTable;
    private JScrollPane                topPanel;
    private JScrollPane                bottomPanel;
    private JSplitPane                 centralDivider;
    private JPanel                     clearAndStatusBottomPanel;
    private JButton                    clearButton;
    private JButton                    infoButton;
    private TableRowSorter<TableModel> sorter;
    private TableRowSorter<TableModel> infoTableSorter;
    private int                        sequence                                 = 0;
    private List<Integer>              ports                                    = new ArrayList<Integer>();
    private JPanel                     clearPanel;
    private List<LocalServer>          localServers                             = new ArrayList<LocalServer>();
    private JTable                     infoTable;
    private DefaultTableModel          infoDtm;
    private JPanel                     infoTablePanel;

    public synchronized static NetworkView getInstance()
    {
        return INSTANCE;
    }

    private NetworkView()
    {
        createComponents();
        configureComponents();
        setContainerLayouts();
        addComponentsInContainerLayouts();
        addListeners();

        setVisible(true);
    }

    private void createComponents()
    {
        this.dtm                       = new DefaultTableModel(new String[0][COLUMN_NAMES.length], COLUMN_NAMES)
                                       {

                                           private static final long serialVersionUID = 1L;

                                           @Override
                                           public boolean isCellEditable(int row, int column)
                                           {
                                               return false;
                                           }

                                       };
        this.sorter                    = new TableRowSorter<TableModel>(this.dtm);
        this.jTable                    = new JTable(this.dtm)
                                       {
                                           private static final long serialVersionUID = 1L;

                                           public Component prepareRenderer(TableCellRenderer renderer, int row, int colIndex)
                                           {
                                               JComponent cell = (JComponent) super.prepareRenderer(renderer, row, colIndex);
                                               if (isCellSelected(row, colIndex))
                                               {
                                                   cell.setBackground(new Color(26, 115, 232));
                                                   cell.setForeground(Color.WHITE);
                                               }
                                               else
                                               {
                                                   if (row % 2 == 0)
                                                   {
                                                       cell.setBackground(new Color(245, 245, 245));
                                                   }
                                                   else
                                                   {
                                                       cell.setBackground(new Color(255, 255, 255));
                                                   }
                                                   int status = StringUtils.toInteger(getValueAt(row, convertColumnIndexToView(STATUS_COLUMN)));
                                                   if (status >= 400 && status <= 599)
                                                   {
                                                       cell.setForeground(ERROR_FONT_COLOR);
                                                   }
                                                   else
                                                   {
                                                       cell.setForeground(FONT_COLOR);
                                                   }
                                               }
                                               cell.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(205, 205, 205)));
                                               return cell;
                                           }

                                       };
        this.infoDtm                   = new DefaultTableModel(
                new Object[0][INFO_COLUMN_NAMES.length],
                INFO_COLUMN_NAMES)
                                       {
                                           private static final long serialVersionUID = 1L;

                                           @Override
                                           public boolean isCellEditable(int row, int column)
                                           {
                                               return false;
                                           }

                                       };
        this.infoTableSorter           = new TableRowSorter<TableModel>(this.infoDtm);
        this.infoTable                 = new JTable(this.infoDtm)
                                       {
                                           private static final long serialVersionUID = 1L;

                                           public Component prepareRenderer(TableCellRenderer renderer, int row, int colIndex)
                                           {
                                               JComponent cell   = (JComponent) super.prepareRenderer(renderer, row, colIndex);
                                               boolean    online = (" " + Constants.ONLINE).equals(getValueAt(row, convertColumnIndexToView(MODE_COLUMN)));
                                               if (isCellSelected(row, colIndex))
                                               {
                                                   if (online)
                                                   {
                                                       cell.setBackground(new Color(26, 115, 232));
                                                       // cell.setBackground(new Color(245, 245, 255));
                                                       cell.setForeground(Color.WHITE);
                                                   }
                                                   else
                                                   {
                                                       cell.setBackground(new Color(232, 115, 26));
                                                       // cell.setBackground(new Color(232, 115, 26));
                                                       cell.setForeground(Color.WHITE);
                                                   }
                                               }
                                               else
                                               {
                                                   if (row % 2 == 0)
                                                   {
                                                       if (online)
                                                       {
                                                           cell.setBackground(new Color(232, 242, 255));
                                                           // cell.setBackground(new Color(235, 235, 255));
                                                       }
                                                       else
                                                       {
                                                           cell.setBackground(new Color(255, 242, 232));
                                                           // cell.setBackground(new Color(255, 190, 101));
                                                           // cell.setBackground(new Color(255, 235, 235));
                                                       }
                                                   }
                                                   else
                                                   {
                                                       if (online)
                                                       {
                                                           cell.setBackground(new Color(212, 222, 235));
                                                           // cell.setBackground(new Color(245, 245, 255));
                                                       }
                                                       else
                                                       {
                                                           cell.setBackground(new Color(235, 222, 212));
                                                           // cell.setBackground(new Color(255, 240, 151));
                                                           // cell.setBackground(new Color(255, 245, 245));
                                                       }
                                                   }
                                                   cell.setForeground(FONT_COLOR);
                                               }
                                               cell.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(205, 205, 205)));
                                               return cell;
                                           }

                                       };
        this.textArea                  = new JTextArea();
        this.topPanel                  = new JScrollPane();
        this.bottomPanel               = new JScrollPane();
        this.centralDivider            = new JSplitPane();
        this.clearAndStatusBottomPanel = new JPanel();
        this.clearPanel                = new JPanel();
        this.clearButton               = new JButton("Limpar");
        this.infoButton                = new JButton("Informações");
        this.infoTablePanel            = new JPanel();
    }

    private void configureComponents()
    {
        setToFixedLenghtFont(this.textArea);
        this.textArea.setEditable(false);

        hideTableColumn(this.jTable, CONTENT_COLUMN);
        hideTableColumn(this.jTable, REQUEST_ID_COLUMN);
        hideTableColumn(this.jTable, RESPONSE_ID_COLUMN);
        hideTableColumn(this.jTable, THREAD_ID_COLUMN);

        this.jTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        this.jTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        this.jTable.setShowGrid(false);
        this.jTable.setRowSorter(this.sorter);
        autoResizeColumns(this.jTable, COLUMNS_LABELS_REFERENCES_FOR_WIDTH);

        this.infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        this.infoTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.infoTable.setRowSorter(this.infoTableSorter);
        autoResizeColumns(this.infoTable, INFO_COLUMNS_LABELS_REFERENCES_FOR_WIDTH);

        this.topPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.topPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.bottomPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.bottomPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.centralDivider.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.centralDivider.setContinuousLayout(true);
        this.centralDivider.setDividerSize(4);
        this.centralDivider.setDividerLocation(0.5);
        this.centralDivider.setResizeWeight(0.5);

        this.topPanel.setPreferredSize(this.textArea.getMinimumSize());
        this.bottomPanel.setPreferredSize(this.textArea.getMinimumSize());

        this.infoTablePanel.setVisible(false);

        int windowWidth  = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
        int windowHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()) / 2;
        this.setSize(windowWidth, windowHeight);
        // this.setExtendedState(this.getExtendedState() |
        // JFrame.MAXIMIZED_BOTH);

        this.setLocation(0, 0);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    private void setToFixedLenghtFont(final JComponent component)
    {
        Font f = new Font("Courier New", Font.PLAIN, this.textArea.getFont().getSize());
        if (f != null)
        {
            component.setFont(f);
        }
    }

    private void hideTableColumn(final JTable jTable, final int column)
    {
        jTable.getColumnModel().getColumn(column).setWidth(0);
        jTable.getColumnModel().getColumn(column).setMinWidth(0);
        jTable.getColumnModel().getColumn(column).setPreferredWidth(0);
        jTable.getColumnModel().getColumn(column).setMaxWidth(0);
    }

    private void autoResizeColumns(final JTable table, String[] columnsLabelsReferencesForWidth)
    {
        int minWidth = 0;
        for (int column = 0, size2 = table.getColumnCount() - 1; column < size2; column++)
        {
            if (columnsLabelsReferencesForWidth[column] == null || "".equals(columnsLabelsReferencesForWidth[column].trim()))
            {
                continue;
            }
            double maxWidth = 0;
            // rows width
            for (int row = 0, size1 = table.getRowCount(); row < size1; row++)
            {
                Object    value         = table.getValueAt(row, column);
                Dimension preferredSize = table.getCellRenderer(row, column)
                        .getTableCellRendererComponent(table, " " + value + " ", false, false, row, column)
                        .getMinimumSize();
                maxWidth = Math.max(maxWidth, preferredSize.getWidth());
            }
            // column header width
            Object    value         = columnsLabelsReferencesForWidth[table.convertColumnIndexToModel(column)];
            Dimension preferredSize = table.getCellRenderer(-1, column)
                    .getTableCellRendererComponent(table, " " + value + " ", false, false, -1, column)
                    .getMinimumSize();
            maxWidth = Math.max(maxWidth, preferredSize.getWidth());

            if (minWidth == 0)
            {
                minWidth = (int) table.getCellRenderer(-1, column)
                        .getTableCellRendererComponent(table, " ABCDE ", false, false, -1, column)
                        .getMinimumSize().getWidth();
            }

            table.getColumnModel().getColumn(column).setWidth((int) maxWidth);
            table.getColumnModel().getColumn(column).setPreferredWidth((int) maxWidth);
            table.getColumnModel().getColumn(column).setMinWidth(minWidth);
            // table.getColumnModel().getColumn(column).setMaxWidth((int) maxWidth);
        }
    }

    private void setContainerLayouts()
    {
        getContentPane().setLayout(new BorderLayout());
        this.clearAndStatusBottomPanel.setLayout(new BoxLayout(this.clearAndStatusBottomPanel, BoxLayout.Y_AXIS));
        this.clearPanel.setLayout(new BoxLayout(this.clearPanel, BoxLayout.X_AXIS));
        this.infoTablePanel.setLayout(new BoxLayout(this.infoTablePanel, BoxLayout.Y_AXIS));
    }

    private void addComponentsInContainerLayouts()
    {
        this.topPanel.setViewportView(this.jTable);
        this.bottomPanel.setViewportView(this.textArea);
        this.centralDivider.setTopComponent(this.topPanel);
        this.centralDivider.setBottomComponent(this.bottomPanel);

        this.clearPanel.add(Box.createVerticalGlue());
        this.clearPanel.add(this.clearButton);
        this.clearPanel.add(this.infoButton);
        this.clearPanel.add(Box.createVerticalGlue());

        this.infoTablePanel.add(this.infoTable.getTableHeader());
        this.infoTablePanel.add(this.infoTable);

        this.clearAndStatusBottomPanel.add(this.clearPanel);
        this.clearAndStatusBottomPanel.add(this.infoTablePanel);

        getContentPane().add(this.centralDivider, BorderLayout.CENTER);
        getContentPane().add(this.clearAndStatusBottomPanel, BorderLayout.SOUTH);
    }

    private void addListeners()
    {
        this.jTable.getSelectionModel().addListSelectionListener(new ClickOnCellAction());
        this.addWindowListener(new CloseAction());
        this.clearButton.addActionListener(new ClearAction());
        this.infoButton.addActionListener(new ExecutionDetailsAction());
    }

    public synchronized void clear()
    {
        this.sequence = 0;
        final int rowCount = this.dtm.getRowCount();
        for (int i = 0; i < rowCount; i++)
        {
            this.dtm.removeRow(0);
        }
        this.textArea.setText("");
        this.jTable.clearSelection();
    }

    private synchronized void clearInfo()
    {
        final int rowCount = this.infoDtm.getRowCount();
        for (int i = 0; i < rowCount; i++)
        {
            this.infoDtm.removeRow(0);
        }
        this.infoTable.clearSelection();
    }

    public synchronized void detail()
    {
        int selectedRow = this.jTable.getSelectedRow();
        if (selectedRow != -1)
        {
            this.textArea.setText((String) this.jTable.getValueAt(selectedRow, this.jTable.convertColumnIndexToView(CONTENT_COLUMN)));
            this.textArea.setSelectionStart(0);
            this.textArea.setSelectionEnd(0);
            int status = StringUtils.toInteger(this.jTable.getValueAt(selectedRow, this.jTable.convertColumnIndexToView(STATUS_COLUMN)));
            if (status >= 400 && status <= 599)
            {
                this.textArea.setForeground(ERROR_FONT_COLOR);
            }
            else
            {
                this.textArea.setForeground(FONT_COLOR);
            }
        }

    }

    public synchronized void toggleExecutionDetails()
    {
        this.infoTablePanel.setVisible(!this.infoTablePanel.isVisible());
    }

    public synchronized void addRequest(final ClassicHttpRequest request, final String fromUrl, String toUrl)
    {
        String recorded     = "?";
        String urlPath      = request.getPath();
        String method       = request.getMethod();
        String status       = "?";
        String responseType = "?";
        this.sequence++;

        final String[] newRow = new String[COLUMN_NAMES.length];
        newRow[CACHED_COLUMN]       = " " + recorded;
        newRow[METHOD_COLUMN]       = " " + method;
        newRow[STATUS_COLUMN]       = " " + status;
        newRow[URL_PATH_COLUMN]     = " " + urlPath;
        newRow[CONTENT_TYPE_COLUMN] = " " + responseType;
        newRow[CONTENT_COLUMN]      = getContent(request, fromUrl, toUrl);
        newRow[REQUEST_ID_COLUMN]   = StringUtils.getHashCode(request);
        newRow[RESPONSE_ID_COLUMN]  = null;
        newRow[SEQUENCE_COLUMN]     = " " + StringUtils.fillLeftWithZeros(Integer.toString(this.sequence));
        newRow[THREAD_ID_COLUMN]    = StringUtils.getHashCode(Thread.currentThread());

        this.dtm.addRow(newRow);
        int value = this.topPanel.getVerticalScrollBar().getMaximum();
        this.topPanel.getVerticalScrollBar().setValue(value);
    }

    public synchronized void updateRequestWithResponse(final ClassicHttpRequest request, final ClassicHttpResponse response, final Boolean recorded, final String fromUrl, final String toUrl)
    {
        String recordedText = "?";
        if (recorded != null)
        {
            if (recorded)
            {
                recordedText = "sim";
            }
            else
            {
                recordedText = "não";
            }
        }
        final String urlPath      = request.getPath();
        final String method       = request.getMethod().toString();
        final String status       = response == null ? "?" : Integer.toString(response.getCode());
        String       responseType = "?";
        try
        {
            responseType = response.getHeader("Content-Type") == null ? "?" : response.getHeader("Content-Type").getValue();
        }
        catch (final Throwable e)
        {
            // ignore
        }

        final String[] newRow = new String[COLUMN_NAMES.length];
        newRow[CACHED_COLUMN]       = " " + recordedText;
        newRow[METHOD_COLUMN]       = " " + method;
        newRow[STATUS_COLUMN]       = " " + status;
        newRow[URL_PATH_COLUMN]     = " " + urlPath;
        newRow[CONTENT_TYPE_COLUMN] = " " + responseType;
        newRow[CONTENT_COLUMN]      = getContent(recorded, request, response, fromUrl, toUrl);
        newRow[REQUEST_ID_COLUMN]   = StringUtils.getHashCode(request);
        newRow[RESPONSE_ID_COLUMN]  = StringUtils.getHashCode(response);
        newRow[SEQUENCE_COLUMN]     = null;
        newRow[THREAD_ID_COLUMN]    = StringUtils.getHashCode(Thread.currentThread());

        updateResponse(newRow);
    }

    private String getContent(final ClassicHttpRequest request, final String fromUrl, String toUrl)
    {
        return getContent(null, request, (ClassicHttpResponse) null, fromUrl, toUrl);
    }

    private String getContent(final Boolean recorded, final ClassicHttpRequest request, final ClassicHttpResponse response, final String fromUrl, final String toUrl)
    {
        final StringBuffer sb = new StringBuffer();

        // GERAL
        sb.append("Geral");
        sb.append("\n\tRequest URL: " + fromUrl);
        if (fromUrl != null && !fromUrl.equals(toUrl))
        {
            sb.append("\n\tProxied To:  " + toUrl);
        }
        sb.append("\n\tRequest Method: " + request.getMethod());
        sb.append("\n\tStatus Code: ");
        sb.append(recorded == null ? "?" : response == null ? "<<SEM RESPONSE>>" : response.getCode());

        // RESPONSE
        sb.append("\n\nResponse Headers");
        appendHeaders(sb, response);
        sb.append("\n\nResponse Body");
        appendBody(sb, response);

        // REQUEST
        sb.append("\n\nRequest Headers");
        appendHeaders(sb, request);
        sb.append("\n\nRequest Body");
        appendBody(sb, request);

        return sb.toString();
    }

    private void appendHeaders(final StringBuffer sb, final ClassicHttpResponse response)
    {
        if (response == null)
        {
            sb.append("\n\t<<SEM RESPONSE>>");
        }
        else
        {
            appendHeaders(sb, response.getHeaders());
        }
    }

    private void appendHeaders(StringBuffer sb, ClassicHttpRequest request)
    {
        if (request == null)
        {
            sb.append("\n\t<<SEM REQUEST>>");
        }
        else
        {
            appendHeaders(sb, request.getHeaders());
        }
    }

    private void appendHeaders(StringBuffer sb, Header[] headers)
    {
        if (headers != null)
        {
            for (Header header : headers)
            {
                sb.append("\n\t" + header.getName() + ": " + header.getValue());
            }
        }
    }

    private void appendBody(StringBuffer sb, ClassicHttpResponse response)
    {
        if (response == null || response.getEntity() == null)
        {
            sb.append("\n<<SEM RESPONSE>>");
        }
        else
        {
            String content = null;
            if (response.getEntity().getContentLength() > Constants.DISPLAY_MAX_BODY_LENGTH
                    && response.getCode() != HttpStatus.SC_NOT_FOUND)
            {
                String  mappingFileName;
                boolean wasRecorded = true;
                try
                {
                    Header header = response.getHeader(Constants.MAPPING_FILE_NAME);
                    if (header == null)
                    {
                        wasRecorded = false;
                    }
                    mappingFileName = header.getValue();
                }
                catch (final Throwable e)
                {
                    mappingFileName = "<<Erro ao obter nome do arquivo de mapeamento [" + StringUtils.getErrorMessage(e) + "]>>";
                }
                if (!wasRecorded)
                {
                    content = "Arquivo muito grande para ser exibido (>" +
                            Constants.DISPLAY_MAX_BODY_LENGTH + "bytes).";
                }
                else
                {
                    content = "Arquivo muito grande para ser exibido (>" +
                            Constants.DISPLAY_MAX_BODY_LENGTH +
                            "bytes). Consulte o arquivo de mapeamento " +
                            mappingFileName +
                            " para obter mais detalhes da resposta retornada que foi gravada em disco";
                }
            }
            else
            {
                content = StringUtils.getContent(response.getEntity());
            }
            sb.append("\n" + content);
        }
    }

    private void appendBody(StringBuffer sb, ClassicHttpRequest request)
    {
        if (request == null)
        {
            sb.append("\n<<SEM REQUEST>>");
        }
        else
        {
            sb.append("\n" + StringUtils.getContent(request.getEntity()));
        }
    }

    private void updateResponse(final String[] newRow)
    {
        final int rowCount      = this.dtm.getRowCount();
        boolean   requestFinded = false;
        for (int i = rowCount - 1; i >= 0; i--)
        {
            // ATUALIZA OS DADOS NA LINHA ONDE TEM O MESMO ID DE REQUISIÇÃO
            if (this.dtm.getValueAt(i, REQUEST_ID_COLUMN).equals(newRow[REQUEST_ID_COLUMN]))
            {
                requestFinded = true;
                for (int j = 0; j < newRow.length; j++)
                {
                    if (newRow[j] != null)
                    {
                        this.dtm.setValueAt(newRow[j], i, j);
                    }
                    else
                    {
                        this.dtm.setValueAt(this.dtm.getValueAt(i, j), i, j);
                    }
                }
                break;
            }
        }
        if (!requestFinded)
        {
            for (int i = rowCount - 1; i >= 0; i--)
            {
                // ATUALIZA OS DADOS NA LINHA ONDE TEM O MESMO ID DE THREAD,
                // CASO O ID DA REQUISIÇÃO NÃO SEJA ENCONTRADO
                if (this.dtm.getValueAt(i, THREAD_ID_COLUMN).equals(newRow[THREAD_ID_COLUMN]))
                {
                    requestFinded = true;
                    for (int j = 0; j < newRow.length; j++)
                    {
                        if (newRow[j] != null)
                        {
                            this.dtm.setValueAt(newRow[j], i, j);
                        }
                        else
                        {
                            this.dtm.setValueAt(this.dtm.getValueAt(i, j), i, j);
                        }
                    }
                    break;
                }
            }
        }
        if (!requestFinded)
        {
            // INCLUI NOVA LINHA CASO O ID DA REQUISIÇÃO NÃO SEJA ENCONTRADO E O
            // ID DE THREAD NÃO SEJA ENCONTRADO
            this.sequence++;
            final String sequenceText = StringUtils.fillLeftWithZeros(Integer.toString(this.sequence));
            newRow[SEQUENCE_COLUMN] = sequenceText;
            this.dtm.addRow(newRow);
        }
        detail();
    }

    private synchronized void addPort(final Integer port)
    {
        if (ports.indexOf(port) == -1)
        {
            ports.add(port);
        }
        formatTitle();
    }

    public synchronized void addLocalServer(final LocalServer localServer)
    {
        try
        {
            addPort(HttpUtils.getPort(new URL(localServer.getHostname())));
        }
        catch (final Throwable e)
        {
            // ignore
        }
        if (this.localServers.indexOf(localServer) != -1)
        {
            return;
        }
        this.localServers.add(localServer);
        clearInfo();
        for (final LocalServer server : this.localServers)
        {
            for (Entry<String, String> mapping : server.getUrlMappings().entrySet())
            {
                String         fromUrl = mapping.getKey();
                String         toUrl   = mapping.getValue();

                final String[] newRow  = new String[INFO_COLUMN_NAMES.length];
                if (server.isOnline())
                {
                    newRow[MODE_COLUMN] = " " + Constants.ONLINE;
                }
                else
                {
                    newRow[MODE_COLUMN] = " " + Constants.OFFLINE;
                }
                if (server.getRecordingDirectory() != null)
                {
                    newRow[RECORDING_DIRECTORY_COLUMN] = " " + server.getRecordingDirectory();
                }
                newRow[LOCAL_SERVER_COLUMN] = " " + fromUrl;
                if (server.isOnline())
                {
                    newRow[PROXIED_SERVER_COLUMN] = " " + toUrl;
                }
                this.infoDtm.addRow(newRow);
            }
        }
        autoResizeColumns(this.infoTable, INFO_COLUMNS_LABELS_REFERENCES_FOR_WIDTH);
    }

    private void formatTitle()
    {
        setTitle(" Ouvindo nas portas "
                + ports.toString()
                        .replace("[", "")
                        .replace("]", ""));
    }

}
