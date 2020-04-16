package com.cursoandroid.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.cursoandroid.whatsapp.R;
import com.cursoandroid.whatsapp.activity.ChatActivity;
import com.cursoandroid.whatsapp.activity.GrupoActivity;
import com.cursoandroid.whatsapp.adapter.ContatosAdapter;
import com.cursoandroid.whatsapp.adapter.ConversasAdapter;
import com.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import com.cursoandroid.whatsapp.helper.RecyclerItemClickListener;
import com.cursoandroid.whatsapp.helper.UsuarioFirebase;
import com.cursoandroid.whatsapp.model.Conversa;
import com.cursoandroid.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter contatosAdapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configurações iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();


        //configurar adapter
        contatosAdapter = new ContatosAdapter(listaContatos, getActivity() );



        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true );
        recyclerViewListaContatos.setAdapter( contatosAdapter );

        //Configura evento de clique no recyclerview
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = contatosAdapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get( position );
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if ( cabecalho ) {

                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity( i );

                                } else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);
                                    startActivity( i );
                                    listaContatos.clear();
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        adicionarMenuNovoGrupo();

        return view;
    }

    public void adicionarMenuNovoGrupo() {

        /*Define usuário com email vazio
         * em caso de e-mail vazio e usuário será utilizado como
         * cabecalho, exibindo novo grupo */
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listaContatos.add( itemGrupo );
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();;
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerContatos );
    }

    public void limparListaContatos() {

        listaContatos.clear();


    }

    public void pesquisarContatos(String texto) {

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for ( Usuario usuario : listaContatos ) {

            String nome = usuario.getNome().toLowerCase();
            if (nome.contains( texto )) {
                listaContatosBusca.add(usuario);
            }


        }

        contatosAdapter = new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter(contatosAdapter);
        contatosAdapter.notifyDataSetChanged();


    }

    public void recarregarContatos() {
        contatosAdapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter(contatosAdapter);
        contatosAdapter.notifyDataSetChanged();
    }

    public void recuperarContatos() {


       valueEventListenerContatos =  usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                limparListaContatos();
                adicionarMenuNovoGrupo();

                for ( DataSnapshot dados: dataSnapshot.getChildren() ) {

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if( !emailUsuarioAtual.equals( usuario.getEmail() ) ) {
                        listaContatos.add(usuario);
                    }
                }

                contatosAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
